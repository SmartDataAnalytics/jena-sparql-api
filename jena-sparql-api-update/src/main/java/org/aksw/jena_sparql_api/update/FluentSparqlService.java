package org.aksw.jena_sparql_api.update;

import java.util.Collection;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;

import com.google.common.base.Function;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class FluentSparqlService {
    //private FluentQueryExecutionFactory fluentQef =
    //private FluentUpdateExecutionFactory fluentUef;
    protected SparqlService sparqlService;

    private FluentQueryExecutionFactoryEndable fluentQef;
    private FluentUpdateExecutionFactoryEndable fluentUef;

    public FluentSparqlService(SparqlService sparqlService) {
        this.sparqlService = sparqlService;
        this.fluentQef = new FluentQueryExecutionFactoryEndable(this);
        this.fluentUef = new FluentUpdateExecutionFactoryEndable(this);
    }

    public FluentQueryExecutionFactoryEndable configureQuery() {
        return fluentQef;
    }

    public FluentUpdateExecutionFactoryEndable configureUpdate() {
        return fluentUef;
    }

    public SparqlService create() {
        //QueryExecutionFactory qef = fluentQef.create();
        //UpdateExecutionFactory uef = fluentUef.create();
        return sparqlService;
    }

    public <T extends UpdateExecutionFactory & DatasetListenable> FluentSparqlService withUpdateListeners(Function<SparqlService, T> updateStrategy, Collection<DatasetListener> listeners) {

        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        T uef = updateStrategy.apply(sparqlService);
        uef.getDatasetListeners().addAll(listeners);
        //UpdateContext updateContext = new UpdateContext(sparqlService, batchSize, containmentChecker);

        //UpdateExecutionFactory uef = new UpdateExecutionFactoryEventSource(updateContext);
        sparqlService = new SparqlServiceImpl(qef, uef);

        return this;
    }



    public static FluentSparqlService forModel() {
        Model model = ModelFactory.createDefaultModel();

        QueryExecutionFactory qef = FluentQueryExecutionFactory.model(model).create();
        UpdateExecutionFactory uef = FluentUpdateExecutionFactory.from(model).create();

        FluentSparqlService result = from(qef, uef);

        return result;
    }

    public static FluentSparqlService from(QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        SparqlService sparqlService = new SparqlServiceImpl(qef, uef);
        FluentSparqlService result = from(sparqlService);
        return result;
    }

    public static FluentSparqlService from(SparqlService sparqlService) {
        FluentSparqlService result = new FluentSparqlService(sparqlService);
        return result;
    }

}
