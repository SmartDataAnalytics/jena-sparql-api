package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Prologue;

public class RdfClassFetcher {
    protected Prologue prologue;
    protected QueryExecutionFactory qef;

    public RdfClassFetcher(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    public ListService<Concept, Node, DatasetGraph> prepareList(RdfClass rdfClass, Concept concept) {
        ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);


        return null;
    }


    public MappedConcept<DatasetGraph> getMappedQuery(ResourceShapeBuilder builder, RdfClass rdfClass) {

        Collection<RdfProperty> rdfProperties = rdfClass.getRdfProperties();

        for(RdfProperty rdfProperty : rdfProperties) {
            processProperty(builder, rdfProperty);
        }

        ResourceShape shape = builder.getResourceShape();
        MappedConcept<DatasetGraph> result = ResourceShape.createMappedConcept2(shape, null);
        return result;
    }


    public void processProperty(ResourceShapeBuilder builder, RdfProperty rdfProperty) {
        Relation relation = rdfProperty.getRelation();
        builder.outgoing(relation);

        //rdfProperty.getTargetRdfClass()
    }

}
