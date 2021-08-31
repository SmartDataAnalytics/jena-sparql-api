package org.aksw.difs.engine;

import org.aksw.jena_sparql_api.arq.core.OpExecutorWithCustomServiceExecutors;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;

// TODO We need to revise OpExecutor such that quad patterns are forwarded to the dataset graph
// The default implementation is graph (not named graph) centric and ALWAYS iterates all named graphs
// when GRAPH ?foo is used.
public class OpExecutorQuadForm
    extends OpExecutorWithCustomServiceExecutors
{
    public static final OpExecutorFactory FACTORY = OpExecutorQuadForm::new;

    protected OpExecutorQuadForm(ExecutionContext execCxt) {
        super(execCxt);
    }

    protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input) {
        // Convert to BGP forms to execute in this graph-centric engine.
        if (quadPattern.isDefaultGraph() && execCxt.getActiveGraph() == execCxt.getDataset().getDefaultGraph()) {
            // Note we tested that the containing graph was the dataset's
            // default graph.
            // Easy case.
            OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
            return execute(opBGP, input) ;
        }

        // Not default graph - (graph .... )
        return QueryIterBlockQuads.create(input, quadPattern.getPattern(), execCxt) ;
//        OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
//        OpGraph op = new OpGraph(quadPattern.getGraphNode(), opBGP) ;
//        return execute(op, input) ;
    }

}