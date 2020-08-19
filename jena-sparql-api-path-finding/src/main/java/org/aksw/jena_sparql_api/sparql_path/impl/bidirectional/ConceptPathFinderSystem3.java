package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderFactorySummaryBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearchSparqlBase;
import org.aksw.jena_sparql_api.sparql_path.core.PathConstraint3;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.lang.arq.ParseException;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ConceptPathFinderSystem3
    implements ConceptPathFinderSystem
{
    @Override
    public Single<Model> computeDataSummary(SparqlQueryConnection dataConnection) {
        InputStream in = ConceptPathFinderBidirectionalUtils.class.getClassLoader().getResourceAsStream("concept-path-finder-type-local.sparql");
        //Stream<SparqlStmt> stmts;
        Flowable<SparqlStmt> stmts;
        stmts = Flowable.fromIterable(() -> {
            try {
                return SparqlStmtUtils.parse(in, SparqlStmtParserImpl.create(Syntax.syntaxARQ, true));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        });

        Single<Model> result = stmts
            //.peek(System.out::println)
            .filter(SparqlStmt::isQuery)
            .map(SparqlStmt::getAsQueryStmt)
            .map(SparqlStmtQuery::getQuery)
            .filter(q -> q.isConstructType())
            .map(dataConnection::queryConstruct)
            .toList()
            .map(list -> {
                Model r = ModelFactory.createDefaultModel();
                list.forEach(r::add);
                return r;
            });

        return result;
    }

    @Override
    public ConceptPathFinderFactoryBidirectional<?> newPathFinderBuilder() {
        return new ConceptPathFinderFactoryBidirectional<>();
    }

    public static class ConceptPathFinderFactoryBidirectional<T extends ConceptPathFinderFactoryBidirectional<T>>
        extends ConceptPathFinderFactorySummaryBase<T>
    {
        // NOTE We could add more specific attributes here if we wanted
        @Override
        public ConceptPathFinder build() {
            return new ConceptPathFinderBase(dataSummary.getGraph(), dataConnection) {
                @Override
                public PathSearch<SimplePath> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept) {

                    return new PathSearchSparqlBase(dataConnection, sourceConcept, targetConcept) {

                        @Override
                        public Flowable<SimplePath> execCore() {
//							pathValidators,
//							ConceptPathFinderBidirectionalUtils::convertGraphPathToSparqlPath3
                            Flowable<SimplePath> result = ConceptPathFinderBidirectionalUtils
                                .findPathsCore(dataConnection,
                                        sourceConcept,
                                        targetConcept,
                                        maxResults,
                                        maxLength,
                                        dataSummary,
                                        shortestPathsOnly,
                                        simplePathsOnly,
                                        pathValidators,
                                        new PathConstraint3(),
                                        ConceptPathFinderBidirectionalUtils::convertGraphPathToSparqlPath3);

                            return result;
                        }
                    };
                }
            };
        }
    }

    public static void main(String[] args) throws Exception {
        Model m = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/faceted-browsing-benchmark-parent/faceted-browsing-benchmark-parent/faceted-browsing-benchmark-v2-parent/faceted-browsing-benchmark-v2-core/src/main/resources/path-data-simple.ttl");

        try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(m))) {
            ConceptPathFinderSystem system = new ConceptPathFinderSystem3();
            Model dataSummary = system.computeDataSummary(conn).blockingGet();

            ConceptPathFinder pathFinder = system.newPathFinderBuilder()
                .setDataConnection(conn)
                .setDataSummary(dataSummary)
                .build();


            List<SimplePath> paths = pathFinder.createSearch(ConceptUtils.createSubjectConcept(), ConceptUtils.createSubjectConcept())
                .setMaxLength(1l)
                .exec()
                .toList()
                .blockingGet();

            System.out.println("Paths: " + paths);
        }
    }


    public static void main3(String[] args) throws Exception {
        DatasetDescription datasetDescription = new DatasetDescription();
        //datasetDescription.addDefaultGraphURI("http://dbpedia.org/wkd_uris");
        datasetDescription.addDefaultGraphURI("http://dbpedia.org");

        try(RDFConnection conn = wrapWithDatasetAndXmlContentType("http://localhost:8890/sparql", datasetDescription)) {
            ConceptPathFinderSystem system = new ConceptPathFinderSystem3();
            Model model = system.computeDataSummary(conn).blockingGet();

            RDFDataMgr.write(new FileOutputStream("/home/raven/dbpedia-data-summary.ttl"), model, RDFFormat.TURTLE_PRETTY);
        }
    }

    public static RDFConnection wrapWithDatasetAndXmlContentType(String url, DatasetDescription datasetDescription) {
        RDFConnection rawConn = RDFConnectionFactory.connect("http://localhost:8890/sparql");
        RDFConnection result = RDFConnectionFactoryEx.wrapWithDatasetAndXmlContentType(rawConn, datasetDescription);
        return result;
    }


    public static void main2(String[] args) {
        DatasetDescription datasetDescription = new DatasetDescription();
        //datasetDescription.addDefaultGraphURI("http://dbpedia.org/wkd_uris");
        datasetDescription.addDefaultGraphURI("http://project-hobbit.eu/benchmark/fbb2/");

        try(RDFConnection conn = wrapWithDatasetAndXmlContentType("http://localhost:8890/sparql", datasetDescription)) {


            //RDFConnection baseConn = RDFConnectionFactory.connect(DatasetFactory.create());

            // Wrap the connection to use a different content type for queries...
            // Jena rejects some of Virtuoso's json output

//			ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
            ConceptPathFinderSystem system = new ConceptPathFinderSystem3();
            Model model = system.computeDataSummary(conn).blockingGet();

            RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);


            ConceptPathFinder conceptPathFinder = system.newPathFinderBuilder()
                .setDataConnection(conn)
                .setDataSummary(model)
                .build();

            PrefixMapping prefixes = PrefixMapping.Extended;

            PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(
                    Concept.create("?src <http://data.nasa.gov/qudt/owl/qudt#unit> ?o", "src", prefixes),
//					Concept.create("?src <http://www.w3.org/ns/ssn/#hasValue> ?o", "src", prefixes),
                    Concept.create("?tgt a <http://www.agtinternational.com/ontologies/lived#CurrentObservation>", "tgt", prefixes));


            List<SimplePath> paths = pathSearch
                    .setMaxPathLength(3)
                    .exec()
                    .timeout(10, TimeUnit.SECONDS)
                    .toList().blockingGet();


            System.out.println("Paths:");
            for(SimplePath path : paths) {
                System.out.println(path);
            }

            System.out.println("done.");
        }

    }
}
