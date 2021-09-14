package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MapPaginatorSparqlQuery
    extends MapPaginatorSparqlQueryBase<Node, Table>
{
    private static final Logger logger = LoggerFactory.getLogger(MapPaginatorSparqlQuery.class);


    protected Query attrQuery;
    protected Var attrVar;
    protected boolean forceSubQuery;


    public MapPaginatorSparqlQuery(SparqlQueryConnection qef, Concept filterConcept, boolean isLeftJoin, Query attrQuery, Var attrVar, boolean forceSubQuery) {
        super(qef, filterConcept, isLeftJoin);
        this.attrQuery = attrQuery;
        this.attrVar = attrVar;
        this.forceSubQuery = forceSubQuery;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {

        if(filterConcept == null) {
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

        Single<Range<Long>> result = SparqlRx.fetchCountConcept(qef, countConcept, itemLimit, null); //rowLimit
        return result;
    }

    @Override
    public Flowable<Entry<Node, Table>> apply(Range<Long> range) {
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


//      Node[] current = {null};
//      Node[] prior = {null};
//      PublishProcessor<Node> boundaryIndicator = PublishProcessor.create();

      return SparqlRx.execSelectRaw(() -> qef.query(query))
              .lift(FlowableOperatorSequentialGroupBy.<Binding, Node, Table>create(
                      b -> b.get(attrVar),
                      groupKey -> new TableN(),
                      Table::addBinding));
//      return ReactiveSparqlUtils.groupByOrdered(
//    		  ReactiveSparqlUtils.execSelect(() -> qef.createQueryExecution(query)),
//    		  b -> b.get(attrVar))
//    	  .map(e -> {
//	    	  Node groupKey = e.getKey();
//	    	  TableN table = new TableN();
//	    	  e.getValue().forEach(table::addBinding);
//
//	    	  return Maps.immutableEntry(groupKey, table);
//	      });

//
//      QueryExecution qe = qef.createQueryExecution(query);
//      //ResultSet rs = qe.execSelect();
//      ResultSet rs = ServiceUtils.forceExecResultSet(qe, query);
//      List<Var> varNames = VarUtils.toList(rs.getResultVars());
//      Iterator<Binding> base = new IteratorResultSetBinding(rs);
//
//      Iterator<Entry<Node, Table>> it = new AbstractIterator<Entry<Node, Table>>() {
//          protected Node currentNode = null;
//          protected Binding lookAhead = null;
//          @Override
//          protected Entry<Node, Table> computeNext() {
//              Table rsp = new TableN(varNames);
//
//              // First time init
//              if(lookAhead == null) {
//                  if(base.hasNext()) {
//                      lookAhead = base.next();
//                  }
//              }
//
//              // Set currentNode from the lookAhead if available
//              if(lookAhead != null) {
//                  rsp.addBinding(lookAhead);
//                  currentNode = lookAhead.get(attrVar);
//                  lookAhead = null;
//              }
//
//              // Iterate until the groupNode no longer equals currentNode
//              Node groupNode = null;
//              while(base.hasNext()) {
//                  lookAhead = base.next();
//                  groupNode = lookAhead.get(attrVar);
//
//                  if(Objects.equal(groupNode, currentNode)) {
//                      rsp.addBinding(lookAhead);
//                      lookAhead = null;
//                  } else {
//                      break;
//                  }
//              }
//
//              Entry<Node, Table> r = lookAhead == null && rsp.isEmpty()
//                      ? null //endOfData()
//                      : new SimpleEntry<>(currentNode, rsp);
//
//              if(r == null) {
//                  endOfData();
//                  // Make sure to close the query execution or we will
//                  // cause starvation in jena's connection pool
//                  qe.close();
//              }
//
//              return r;
//          }
//      };
//
//      Stream<Entry<Node, Table>> result = Streams.stream(it);
//      result.onClose(() -> qe.close());
//
//      return result;
    }
}
