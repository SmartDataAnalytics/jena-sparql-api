package org.aksw.jena_sparql_api.conjure.test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.dcat.ap.utils.DcatUtils;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public class MainConjureSparkComparison {
    private static final Logger logger = LoggerFactory.getLogger(MainConjureSparkComparison.class);

    public static void main(String[] args) throws Exception {

        // TODO Circular init issue with DefaultPrefixes
        // We could use ARQConstants.getGlobalPrefixMap()
        JenaSystem.init();
        Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.prefixes, false);

        Query dcatQuery = parser.apply(
                "	    	       CONSTRUCT {\n" +
                "	    	        ?a ?b ?c .\n" +
                "	    	        ?c ?d ?e\n" +
                "	    	      } {\n" +
                "\n" +
                "	    	        { SELECT DISTINCT ?a {\n" +
                "	    	          ?a dcat:distribution [\n" +
                "	    	            dcat:byteSize ?byteSize\n" +
                "	    	          ]\n" +
                "	    	          FILTER(?byteSize < 100000)\n" +
                "	    	        } LIMIT 1000 }\n" +
                "\n" +
                "	    	        ?a ?b ?c\n" +
                "	    	        OPTIONAL { ?c ?d ?e }\n" +
                "	    	      }").getAsQueryStmt().getQuery();

        Model catalog = RDFDataMgr.loadModel("http://localhost/~raven/conjure.test.dcat.ttl");


        List<Resource> dcatRecords;
        try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(catalog))) {
            dcatRecords = SparqlRx.execConstructGrouped(conn, dcatQuery, Vars.a)
                .map(RDFNode::asResource)
                .toList().blockingGet();
        }


        Model model = ModelFactory.createDefaultModel();

        Op v = OpVar.create(model, "dataRef");
        Op opWorkflow = OpConstruct.create(model, v, parser.apply(
                "CONSTRUCT {\n" +
                "           <env:datasetId>\n" +
                "             eg:predicateReport ?report ;\n" +
                "             .\n" +
                "\n" +
                "           ?report\n" +
                "             eg:entry [\n" +
                "               eg:predicate ?p ;\n" +
                "               eg:numUses ?numTriples ;\n" +
                "               eg:numUniqS ?numUniqS ;\n" +
                "               eg:numUniqO ?numUniqO\n" +
                "             ]\n" +
                "           }\n" +
                "           {\n" +
                "             # TODO Allocate some URI based on the dataset id\n" +
                "             BIND(BNODE() AS ?report)\n" +
                "             { SELECT ?p (COUNT(*) AS ?numTriples) (COUNT(DISTINCT ?s) AS ?numUniqS) (COUNT(DISTINCT ?o) AS ?numUniqO) {\n" +
                "               ?s ?p ?o\n" +
                "             } GROUP BY ?p }\n" +
                "           }").toString());


        HttpResourceRepositoryFromFileSystem repo = HttpResourceRepositoryFromFileSystemImpl.createDefault();
        OpExecutorDefault executor = new OpExecutorDefault(repo, null /* will cause NPE */, new LinkedHashMap<>(), RDFFormat.TURTLE_PRETTY);

        logger.info("Retrieved " + dcatRecords.size() + " urls for processing " + dcatRecords);

        Stream<?> executiveRdd = dcatRecords.stream().map(dcat -> {
            String url = DcatUtils.getFirstDownloadUrl(dcat);
            logger.info("Processing: " + url);
            if(url != null) {

                // Create a copy of the workflow spec and substitute the variables
                Map<String, Op> map = Collections.singletonMap("dataRef", OpDataRefResource.from(model, DataRefUrl.create(model, url)));
                Op effectiveWorkflow = OpUtils.copyWithSubstitution(opWorkflow, map::get);


                // Set up a dataset processing expression
                //logger.info("Conjure spec is:");
                //RDFDataMgr.write(System.err, effectiveWorkflow.getModel(), RDFFormat.TURTLE_PRETTY);

                try(RdfDataPod data = effectiveWorkflow.accept(executor)) {
                    try(RDFConnection conn = data.openConnection()) {
                        // Print out the data that is the process result
                        Model rmodel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");

                        //RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
                    }
                } catch(Exception e) {
                    logger.warn("Failed to process " + url, e);
                }
            }

            return "yay";
        });

        Stopwatch stopwatch = Stopwatch.createStarted();
        long evalResult = executiveRdd.count();
        System.out.println("Processed " + evalResult + " items in " + (stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds");
    }
}
