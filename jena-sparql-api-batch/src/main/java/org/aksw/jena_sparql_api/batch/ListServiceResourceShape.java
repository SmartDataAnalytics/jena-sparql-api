package org.aksw.jena_sparql_api.batch;

import java.util.List;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ListServiceResourceShape
    implements ListService<Concept, Entry<Resource, Model>>
{
    private QueryExecutionFactory qef;
    private ResourceShape resourceShape;

    @Override
    public List<Entry<Resource, Model>> fetchData(Concept concept, Long limit, Long offset) {

        Query query = ResourceShape.createQuery(resourceShape, concept);
        QueryExecution qe = qef.createQueryExecution(query);
        Model model = qe.execConstruct();
    }

    @Override
    public CountInfo fetchCount(Concept concept, Long itemLimit) {
        ListServiceConcept.createQueryCount(concept, itemLimit, rowLimit, resultVar)
    }

}
