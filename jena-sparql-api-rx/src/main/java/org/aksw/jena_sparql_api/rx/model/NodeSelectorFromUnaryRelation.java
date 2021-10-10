package org.aksw.jena_sparql_api.rx.model;

import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

import io.reactivex.rxjava3.core.Flowable;

public class NodeSelectorFromUnaryRelation
    implements NodeSelector
{
    protected UnaryRelation nodeRelation;

    public NodeSelectorFromUnaryRelation(UnaryRelation nodeRelation) {
        super();
        this.nodeRelation = nodeRelation;
    }

    @Override
    public Flowable<RDFNode> streamRDFNodes(Model model) {
        Var var = nodeRelation.getVar();
        Query query = nodeRelation.asQuery();
        return SparqlRx.execConcept(() -> QueryExecutionFactory.create(query, model), var);
    }

    // TODO Add materialize method
}
