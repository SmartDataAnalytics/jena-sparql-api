package org.aksw.jena_sparql_api.shape.lookup;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.MapPaginatorSparqlQueryBase;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.apache.jena.graph.Node;

import com.google.common.collect.Range;

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

    @Override
    public Map<Node, G> fetchMap(Range<Long> range) {
        MapService<Concept, Node, G> ms = MapServiceUtils.createListServiceMappedConcept(qef, mappedConcept, isLeftJoin);
        Map<Node, G> result = ms.fetchData(null, range);
        return result;
    }

    @Override
    public Range<Long> fetchCount(Long itemLimit, Long rowLimit) {
        Range<Long> result = ServiceUtils.fetchCountConcept(qef, mappedConcept.getConcept(), itemLimit, rowLimit);
        return result;
    }

    @Override
    public Stream<Entry<Node, G>> apply(Range<Long> range) {
        return fetchMap(range).entrySet().stream();
    }
}
