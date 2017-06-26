package org.aksw.jena_sparql_api.lookup;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.apache.jena.ext.com.google.common.base.Objects;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;

public class MapPaginatorSparqlQuery
    extends MapPaginatorSparqlQueryBase<Node, ResultSetPart>
{
    private static final Logger logger = LoggerFactory.getLogger(MapPaginatorSparqlQuery.class);


    protected Query attrQuery;
    protected Var attrVar;
    protected boolean forceSubQuery;


    public MapPaginatorSparqlQuery(QueryExecutionFactory qef, Concept filterConcept, boolean isLeftJoin, Query attrQuery, Var attrVar, boolean forceSubQuery) {
        super(qef, filterConcept, isLeftJoin);
        this.attrQuery = attrQuery;
        this.attrVar = attrVar;
        this.forceSubQuery = forceSubQuery;
    }

    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {

        if(filterConcept != null) {
            filterConcept = ConceptUtils.createSubjectConcept();
        }

        Concept countConcept;
        if(this.isLeftJoin) {
            Query query = ConceptUtils.createAttrQuery(this.attrQuery, this.attrVar, this.isLeftJoin, filterConcept, itemLimit, null, this.forceSubQuery);

            countConcept = new Concept(query.getQueryPattern(), this.attrVar);
        } else {
            Concept attrConcept = ( this.forceSubQuery
                ? new Concept(new ElementSubQuery(this.attrQuery), this.attrVar)
                : new Concept(this.attrQuery.getQueryPattern(), this.attrVar) )
                ;

            countConcept = ConceptUtils.createCombinedConcept(attrConcept, filterConcept, true, false, false);
//            console.log('FILTER ' + filterConcept);
//            console.log('ATTR ' + attrConcept);
//            console.log('COUNT ' + countConcept);
//            console.log('ROW ' + rowLimit);
        }

        CountInfo result = ServiceUtils.fetchCountConcept(qef, countConcept, itemLimit, null); //rowLimit
        return result;
    }

    @Override
    public Stream<Entry<Node, ResultSetPart>> apply(Range<Long> range) {
        if(filterConcept != null) {
//          filterConcept = ConceptUtils.createSubjectConcept();
          if(!attrVar.equals(filterConcept.getVar())) {
              filterConcept = ConceptUtils.createRenamedConcept(filterConcept, attrVar);
          }
      }

      // Make the filter concept make use of the attrVar

      //System.out.println(attrQuery);
      //if(true) { throw new RuntimeException("foo"); }

      Long limit = QueryUtils.rangeToLimit(range);
      Long offset = QueryUtils.rangeToOffset(range);

      Query query;
      if(filterConcept == null) {
          query = attrQuery.cloneQuery();//ConceptUtils.createAttrQuery(this.attrQuery, attrVar, this.isLeftJoin, filterConcept, limit, offset, this.forceSubQuery);
          QueryUtils.applySlice(query, offset, limit, false);
      } else {
          query = ConceptUtils.createAttrQuery(this.attrQuery, attrVar, this.isLeftJoin, filterConcept, limit, offset, this.forceSubQuery);
      }

      // Order by the concept variable so we can stream the result set
      SortCondition sc = new SortCondition(new ExprVar(attrVar), Query.ORDER_ASCENDING);
      if(query.getOrderBy() == null) {
          query.addOrderBy(sc);
      } else {
          query.getOrderBy().add(0, sc);
      }


      logger.debug("Query: " + query);
      //System.out.println(query);
      //if(true) {throw new RuntimeException(""); }

      QueryExecution qe = qef.createQueryExecution(query);
      //ResultSet rs = qe.execSelect();
      ResultSet rs = ServiceUtils.forceExecResultSet(qe, query);
      List<String> varNames = rs.getResultVars();
      Iterator<Binding> base = new IteratorResultSetBinding(rs);

      Iterator<Entry<Node, ResultSetPart>> it = new AbstractIterator<Entry<Node, ResultSetPart>>() {
          protected Node currentNode = null;
          protected Binding lookAhead = null;
          @Override
          protected Entry<Node, ResultSetPart> computeNext() {
              ResultSetPart rsp = new ResultSetPart(varNames);

              // First time init
              if(lookAhead == null) {
                  if(base.hasNext()) {
                      lookAhead = base.next();
                  }
              }

              // Set currentNode from the lookAhead if available
              if(lookAhead != null) {
                  rsp.getBindings().add(lookAhead);
                  currentNode = lookAhead.get(attrVar);
                  lookAhead = null;
              }

              // Iterate until the groupNode no longer equals currentNode
              Node groupNode = null;
              while(base.hasNext()) {
                  lookAhead = base.next();
                  groupNode = lookAhead.get(attrVar);

                  if(Objects.equal(groupNode, currentNode)) {
                      rsp.getBindings().add(lookAhead);
                      lookAhead = null;
                  } else {
                      break;
                  }
              }

              Entry<Node, ResultSetPart> r = lookAhead == null && rsp.getBindings().isEmpty()
                      ? endOfData()
                      : new SimpleEntry<>(currentNode, rsp);

              return r;
          }
      };

      Stream<Entry<Node, ResultSetPart>> result = StreamUtils.stream(it);
      result.onClose(() -> qe.close());

      return result;
    }
}
