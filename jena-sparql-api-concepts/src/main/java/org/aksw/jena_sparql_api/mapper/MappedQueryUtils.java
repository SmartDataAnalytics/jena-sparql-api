package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.syntax.Template;

public class MappedQueryUtils {

    public static MappedQuery<DatasetGraph> fromConstructQuery(Query query, Var partitionVar) {
        PartitionedQuery1 partQuery = new PartitionedQuery1Impl(query, partitionVar);
        MappedQuery<DatasetGraph> result = fromConstructQuery(partQuery);
        return result;
    }

    public static MappedQuery<DatasetGraph> fromConstructQuery(PartitionedQuery1 partQuery) {
        MappedQuery<DatasetGraph> result;

        Query query = partQuery.getQuery().cloneQuery();

        if(query.isConstructType()) {
            Template template = query.getConstructTemplate();
            QuadPattern qp = QuadPatternUtils.toQuadPattern(Quad.defaultGraphNodeGenerated, template.getBGP());
            Agg<DatasetGraph> agg = AggDatasetGraph.create(qp);


            query.setQuerySelectType();
            query.setQueryResultStar(false);
            VarExprList project = query.getProject();
            project.getVars().clear();
            project.getExprs().clear();

            Set<Var> vars = agg.getDeclaredVars();

            Var partVar = partQuery.getPartitionVar();
            if(!vars.contains(partVar)) {
                project.add(partVar);
            }

            for(Var var : vars) {
                project.add(var);
            }

//            QuadPattern qp = new QuadPattern();
//            qp.add(new Quad(Quad.defaultGraphNodeGenerated, Vars.s, Vars.p, Vars.o));
//            Agg<DatasetGraph> agg = AggDatasetGraph.create(qp);

            PartitionedQuery1 pq = new PartitionedQuery1Impl(query, partVar);

            result = new MappedQuery<DatasetGraph>(pq, agg);

        } else {
            throw new RuntimeException("Only construct query supported right now");
        }


        return result;
    }
}
