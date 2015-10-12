package org.aksw.jena_sparql_api.mapper;

import org.aksw.jena_sparql_api.utils.Vars;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;

public class MappedQueryUtils {

    public static MappedQuery<DatasetGraph> fromConstructQuery(Query query, Var partitionVar) {
        PartitionedQuery partQuery = new PartitionedQuery(query, partitionVar);
        MappedQuery<DatasetGraph> result = fromConstructQuery(partQuery);
        return result;
    }

    public static MappedQuery<DatasetGraph> fromConstructQuery(PartitionedQuery partQuery) {
        QuadPattern qp = new QuadPattern();
        qp.add(new Quad(Quad.defaultGraphNodeGenerated, Vars.s, Vars.p, Vars.o));
        Agg<DatasetGraph> agg = AggDatasetGraph.create(qp);

        MappedQuery<DatasetGraph> result = new MappedQuery<DatasetGraph>(partQuery, agg);
        return result;
    }
}
