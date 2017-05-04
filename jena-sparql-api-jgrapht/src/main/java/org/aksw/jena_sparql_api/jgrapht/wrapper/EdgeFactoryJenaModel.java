package org.aksw.jena_sparql_api.jgrapht.wrapper;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.EdgeFactory;


public class EdgeFactoryJenaModel
    implements EdgeFactory<RDFNode, Statement>
{
    protected Model model;
    protected Property property;

    public EdgeFactoryJenaModel(Model model, Property property) {
        super();
        this.property = property;
    }

    @Override
    public Statement createEdge(RDFNode sourceVertex, RDFNode targetVertex) {
        Statement result = model.createStatement(sourceVertex.asResource(), property, targetVertex);
        model.add(result);
        return result;
    }
}