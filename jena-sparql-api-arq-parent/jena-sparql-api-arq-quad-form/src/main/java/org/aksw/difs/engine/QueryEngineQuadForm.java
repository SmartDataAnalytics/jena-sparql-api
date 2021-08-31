package org.aksw.difs.engine;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.util.Context;

public class QueryEngineQuadForm
    extends QueryEngineMain
{

    public QueryEngineQuadForm(Op op, DatasetGraph dataset, Binding input, Context context) {
        super(op, dataset, input, context);
    }

    public QueryEngineQuadForm(Query query, DatasetGraph dataset, Binding input, Context context) {
        super(query, dataset, input, context);
    }

    @Override
    protected Op modifyOp(Op op) {
        Op tmp = super.modifyOp(op);
        Op result = Algebra.toQuadForm(tmp);
        return result;
    }


    public static final QueryEngineFactory FACTORY = new QueryEngineQuadFormFactory() ;
    // public static final QueryEngineFactoryProvider PROVIDER = (qu, da, co) -> QueryEngineQuadForm.factory;

    protected static class QueryEngineQuadFormFactory implements QueryEngineFactory
    {
        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context)
        { return true ; }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
        {
            QC.setFactory(context, OpExecutorQuadForm.FACTORY);
            QueryEngineQuadForm engine = new QueryEngineQuadForm(query, dataset, input, context) ;
            return engine.getPlan() ;
        }

        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context)
        { return true ; }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QC.setFactory(context, OpExecutorQuadForm.FACTORY);
            QueryEngineQuadForm engine = new QueryEngineQuadForm(op, dataset, binding, context) ;
            return engine.getPlan() ;
        }
    }
}