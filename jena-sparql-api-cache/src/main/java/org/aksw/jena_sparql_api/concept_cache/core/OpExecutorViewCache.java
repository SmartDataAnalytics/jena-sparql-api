package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.ReplaceConstants;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;


//class CacheConfig {
//    protected ConceptMap conceptMap;
//
//
//}

public class OpExecutorViewCache
    extends OpExecutor
{
    protected Map<Node, QueryExecutionFactoryViewCacheFragment> serviceToQef;


    protected OpExecutorViewCache(ExecutionContext execCxt, Map<Node, QueryExecutionFactoryViewCacheFragment> serviceToQef) {
        super(execCxt);
        this.serviceToQef = serviceToQef;
    }

    @Override
    protected QueryIterator execute(OpService opService, QueryIterator input) {

        Node serviceNode = opService.getService();
        String serviceUri = serviceNode.getURI();

        QueryIterator result;
        if(serviceUri.startsWith("cache://")) {
            //SparqlCacheUtils.
            QueryExecutionFactoryViewCacheFragment qef = serviceToQef.get(serviceNode);
            if(qef == null) {
                throw new RuntimeException("Could not find a query execution factory for " + serviceUri);
            }
            Op tmpOp = opService.getSubOp();

            Collection<Var> vars = OpVars.mentionedVars(tmpOp);
            Map<Node, Var> nodeMap = ElementUtils.createMapFixVarNames(vars);
            NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);

            tmpOp = NodeTransformLib.transform(nodeTransform, tmpOp);

            tmpOp = Transformer.transform(new TransformRemoveGraph(x -> false), tmpOp);
            OpUnion unionOp = (OpUnion)tmpOp;


//            Query tmpQuery = OpAsQuery.asQuery(tmpOp);
//            ElementGroup tmpGroup = (ElementGroup)tmpQuery.getQueryPattern();
//            ElementUnion union = (ElementUnion)tmpGroup.getElements().get(0);//tmpQuery.getQueryPattern();
//
//            Query indexQuery = ((ElementSubQuery)((ElementGroup)union.getElements().get(0)).getElements().get(0)).getQuery();
//            Query executionQuery = ((ElementSubQuery)((ElementGroup)union.getElements().get(1)).getElements().get(0)).getQuery();

            //System.out.println(test);



            Op patternOp = unionOp.getLeft();
            patternOp = Algebra.toQuadForm(patternOp);
            patternOp = ReplaceConstants.replace(patternOp);

            Op executionOp = unionOp.getRight();


            //Query indexQuery = OpAsQuery.asQuery(patternOp);
            Query executionQuery = OpAsQuery.asQuery(executionOp);

            // Get rid of unneccessary GRAPH ?x { ... } elements
            //executionOp = Transformer.transform(new TransformRemoveGraph(x -> false), executionOp);

            //System.out.println("Op is " + executionOp);
            //Optimize.optimize(op, context)

            //Query query = OpAsQuery.asQuery(executionOp);

            //Rename.renameNode(op, oldName, newName)
            // TODO Why is this hack / fix of variable names starting with a '/' necessary? Can we get rid of it?
            executionQuery.setQueryPattern(ElementUtils.fixVarNames(executionQuery.getQueryPattern()));


            //Query query = subQueryElt.getQuery();

            System.out.println("Executing: " + executionQuery);

            QueryExecution qe = qef.createQueryExecution(patternOp, executionQuery);
            ResultSet rs = qe.execSelect();


//            ResultSetViewCache.cacheResultSet(physicalRs, indexVars, indexResultSetSizeThreshold, conceptMap, pqfp);
//
//            QueryExecution qe = qef.createQueryExecution(query);
//            ResultSet rs = qe.execSelect();
//            QueryIterator result = new QueryIteratorResultSet(rs);
//
//            //QueryExecutionFactory
//
//            System.out.println("here");

            result = new QueryIteratorResultSet(rs);

        } else {
            result = super.exec(opService, input);
        }

        return result;
    }


}
