package org.aksw.jena_sparql_api.batch.reader;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;


/**
 * Item reader that reads a SPARQL SELECT query using pagination
 *
 * @author raven
 *
 * @param <T>
 */
public class ItemReaderDatasetGraph
    extends AbstractPaginatedDataItemReader<Entry<Node, DatasetGraph>>
{
    private Concept concept;
    private MapService<Concept, Node, DatasetGraph> listService;

    public ItemReaderDatasetGraph(MapService<Concept, Node, DatasetGraph> listService, Concept concept) {
        setName(this.getClass().getName());
        this.listService = listService;
        this.concept = concept;
    }

    @Override
    protected Iterator<Entry<Node, DatasetGraph>> doPageRead() {
        long limit = (long)this.pageSize;
        long offset = this.page * this.pageSize;

        Map<Node, DatasetGraph> map = listService.fetchData(concept, limit, offset);
        Iterator<Entry<Node, DatasetGraph>> result = map.entrySet().iterator();
        return result;
    }
}
