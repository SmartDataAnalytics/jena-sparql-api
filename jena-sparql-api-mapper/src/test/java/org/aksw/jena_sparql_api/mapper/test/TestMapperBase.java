package org.aksw.jena_sparql_api.mapper.test;

import javax.persistence.EntityManager;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngine;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.jpa.core.EntityManagerJena;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformDatasetDescription;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformLimit;
import org.junit.Before;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetDescription;

public class TestMapperBase {
    protected String graphName;
    protected Dataset ds;
    protected SparqlService sparqlService;
    protected RdfMapperEngineImpl mapperEngine;
    protected EntityManager entityManager;

    @Before
    public void beforeTest() {
        //String graphName = "http://ex.org/graph/";
        graphName = "http://ex.org/graph/";
        ds = DatasetFactory.createMem();
        DatasetDescription dd = DatasetDescriptionUtils.createDefaultGraph(graphName);
        sparqlService = FluentSparqlService.from(ds)
                .config()
                    .configQuery()
                        .withParser(SparqlQueryParserImpl.create())
                    .end()
                    .withDatasetDescription(dd, graphName)
                    .configQuery()
                        .withQueryTransform(F_QueryTransformDatasetDescription.fn)
                        .withQueryTransform(F_QueryTransformLimit.create(1000))
                    .end()
                .end()
                .create();

        mapperEngine = new RdfMapperEngineImpl(sparqlService);
        entityManager = new EntityManagerJena(mapperEngine);
    }
}
