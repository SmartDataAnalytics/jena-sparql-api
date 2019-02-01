package org.aksw.jena_sparql_api_sparql_path2.spark;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api_sparql_path2.playground.main.MainSparqlPath2;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Prologue;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class SparqlPathUtils {


    public static void readModel(Model model, ResourceLoader resourceLoader, String uri, Lang lang) throws IOException {
        Resource resource = resourceLoader.getResource(uri);
        InputStream in = resource.getInputStream();
        RDFDataMgr.read(model, in, lang);
    }

    public static SparqlService createSparqlService(String datasetUri, ResourceLoader resourceLoader, Prologue prologue, QueryExecutionFactory dcatQef) throws IOException {
//      Resource resource = resourceLoader.getResource(datasetUri);
//      InputStream in = resource.getInputStream();
//      Model baseDataModel = ModelFactory.createDefaultModel();
//      RDFDataMgr.read(baseDataModel, in, Lang.TURTLE);
//
//      SparqlService baseDataService = FluentSparqlService
//              .from(baseDataModel)
//              .create();




      SparqlServiceReference ssr = DatasetMapUtils.getSparqlDistribution(dcatQef, datasetUri);
      if(ssr == null) {
          throw new RuntimeException("Could not find information needed to create a sparql service for " + datasetUri);
      }
      SparqlService baseDataService = FluentSparqlService.http(ssr).create();


      SparqlServiceReference pjsSsr = DatasetMapUtils.getPjsDistribution(dcatQef, datasetUri);
      if(pjsSsr == null) {
          throw new RuntimeException("Could not find information needed to create a predicate join summary service for " + pjsSsr);
      }
      SparqlService pjsSummaryService = FluentSparqlService.http(ssr).create();


      if(true) {
          String str = "SELECT DISTINCT  ?x ?z ?s ?p ?o WHERE { { { SELECT  (?s AS ?x) ?s ?p ?o ?z WHERE { ?s  ?p  ?o FILTER ( ?p NOT IN (<http://foo>) ) BIND(false AS ?z) } } } FILTER ( ?x IN (<http://ex.org/r/y3>) ) }";
          ResultSet rs = baseDataService.getQueryExecutionFactory().createQueryExecution(str).execSelect();
          while(rs.hasNext()) {
              System.out.println("GOT: " + rs.nextBinding());
          }
      }
//
//      SparqlService predicateJoinSummaryService = FluentSparqlService
//              .from(JoinSummaryUtils.createPredicateJoinSummary(baseDataService.getQueryExecutionFactory()))
//              .create();
//
//      SparqlService predicateSummaryService = FluentSparqlService
//              .from(JoinSummaryUtils.createPredicateSummary(baseDataService.getQueryExecutionFactory()))
//              .create();


      SparqlStmtParserImpl sparqlStmtParser = SparqlStmtParserImpl.create(new SparqlParserConfig(Syntax.syntaxARQ, prologue));


      SparqlService result = MainSparqlPath2.proxySparqlService(baseDataService, sparqlStmtParser, prologue);

      return result;
  }

}
