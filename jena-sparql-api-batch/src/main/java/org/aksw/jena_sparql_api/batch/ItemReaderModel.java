package org.aksw.jena_sparql_api.batch;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Item reader that reads a SPARQL SELECT query using pagination
 *
 * @author raven
 *
 * @param <T>
 */
public class ItemReaderModel
    extends AbstractPaginatedDataItemReader<Entry<Resource, Model>>
{
    private Concept concept;
    private ListService<Concept, Resource, Model> listService;

    public ItemReaderModel(ListService<Concept, Resource, Model> listService, Concept concept) {
        setName(this.getClass().getName());
        this.listService = listService;
        this.concept = concept;
    }

    @Override
    protected Iterator<Entry<Resource, Model>> doPageRead() {
        long limit = (long)this.pageSize;
        long offset = this.page * this.pageSize;

        Map<Resource, Model> map = listService.fetchData(concept, limit, offset);
        Iterator<Entry<Resource, Model>> result = map.entrySet().iterator();
        return result;
    }
}
