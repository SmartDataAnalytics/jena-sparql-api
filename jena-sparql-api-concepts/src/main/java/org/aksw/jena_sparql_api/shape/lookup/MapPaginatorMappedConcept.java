package org.aksw.jena_sparql_api.shape.lookup;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.lookup.MapPaginatorSparqlQueryBase;
import org.aksw.jena_sparql_api.lookup.MapServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.apache.jena.graph.Node;

import com.google.common.collect.Range;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class MapPaginatorMappedConcept<G>
    extends MapPaginatorSparqlQueryBase<Node, G>
{
    protected ResourceShape resourceShape;
    protected MappedConcept<G> mappedConcept;

    public MapPaginatorMappedConcept(QueryExecutionFactory qef,
            Concept filterConcept,
            boolean isLeftJoin,
            MappedConcept<G> mappedConcept) {
        super(qef, filterConcept, isLeftJoin);
        this.mappedConcept = mappedConcept;
    }

//    @Override
//    public Map<Node, G> fetchMap(Range<Long> range) {
//        MapService<Concept, Node, G> ms = MapServiceUtils.createListServiceMappedConcept(qef, mappedConcept, isLeftJoin);
//        Map<Node, G> result = ms.fetchData(null, range);
//        return result;
//    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
    	Single<Range<Long>> result = ReactiveSparqlUtils.fetchCountConcept(qef, mappedConcept.getConcept(), itemLimit, rowLimit);
    	//Range<Long> result = ServiceUtils.fetchCountConcept(qef, mappedConcept.getConcept(), itemLimit, rowLimit);
        return result;
    }

    @Override
    public Flowable<Entry<Node, G>> apply(Range<Long> range) {
    	Flowable<Entry<Node, G>> result = MapServiceUtils.createListServiceMappedConcept(qef, mappedConcept, isLeftJoin).createPaginator(null).apply(range);
    	return result;
//        return fetchMap(range).entrySet().stream();
    }
}
