package org.aksw.jena_sparql_api.update;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.update.UpdateRequest;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSparqlUpdate {
//
//    private Model model;
//    private UpdateExecutionFactoryEventSource uef;
//    private QueryExecutionFactory qef;
//
//    private UpdateContext updateContext;
//
//
//    private Model csModel;
//    private UpdateExecutionFactory csUef;
//    private QueryExecutionFactory csQef;
//    private SparqlService csSparqlService;
//
//
//    @Before
//    public void init() throws Exception {
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource resource = resolver.getResource("data/data-update-test.nt");
//
//        model = ModelFactory.createDefaultModel();
//        model.read(resource.getInputStream(), null, "N-TRIPLE");
//
//        UpdateExecutionFactory tmp = new UpdateExecutionFactoryModel(model);
//        qef = new QueryExecutionFactoryModel(model);
//
//        SparqlService sparqlService = new SparqlServiceImpl(qef, uef);
//
//        updateContext = new UpdateContext(sparqlService, 128, new QuadContainmentCheckerSimple()); //FunctionQuadDiffUnique.create(qef, )))
//
//        uef = new UpdateExecutionFactoryEventSource(updateContext);
//
//
//        csModel = ModelFactory.createDefaultModel();
//        csQef = new QueryExecutionFactoryModel(csModel);
//        csUef = new UpdateExecutionFactoryModel(csModel);
//
//        csSparqlService = new SparqlServiceImpl(csQef, csUef);
//    }
//
//    @Test
//    public void test1() throws Exception
//    {
//        uef.getDatasetListeners().add(new DatasetListener() {
//
//            @Override
//            public void onPreModify(Diff<Set<Quad>> diff, UpdateContext updateContext) {
//                System.out.println("Diff: " + diff);
//
//                ChangeSetMetadata metadata = new ChangeSetMetadata("Claus", "testing");
//
//                SinkChangeSetWriter sink = new SinkChangeSetWriter(metadata, csSparqlService);
//                sink.send(diff);
//
//
//                csModel.write(System.out, "TURTLE");
//                //ChangeSetUtils.createUpdateRequest(metadata, csQef, csUef, diff, "http://example.org/");
//            }
//        });
//
//
//        String requestStr = "Prefix owl: <http://www.w3.org/2002/07/owl#> Insert { ?s a owl:Thing } Where { ?s a <http://dbpedia.org/ontology/Person> }";
//        UpdateProcessor processor = uef.createUpdateProcessor(UpdateUtils.parse(requestStr));
//        processor.execute();
//
//        //model.write(System.out, "N-TRIPLE");
//    }
//
//    @Test
//    public void test2() throws Exception
//    {
//        test1();
//
//        String requestStr = "Prefix owl: <http://www.w3.org/2002/07/owl#> Delete { ?s a owl:Thing } Where { ?s a <http://dbpedia.org/ontology/Person> }";
//        UpdateRequest updateRequest = UpdateUtils.parse(requestStr);
//        UpdateProcessor processor = uef.createUpdateProcessor(updateRequest);
//        processor.execute();
//    }


    @Test
    public void test3() throws Exception
    {
        // Define the listeners
        List<DatasetListener> listeners = Collections.<DatasetListener>singletonList(new DatasetListener() {
            @Override
            public void onPreModify(Diff<Set<Quad>> diff, UpdateContext updateContext) {
                // Print out any changes to the console
                System.out.println(diff);
            }
        });

        // The fluent API offers convenient construction of common configurations
        // However, should you need more flexibility, you can always create a custom SparqlService decorators.
        SparqlService sparqlService = FluentSparqlService
            .forModel()
            .config()
                .withUpdateListeners(new UpdateStrategyEventSource(), listeners)
            .end()
            .create();

        // Perform the request - the listeners will be notified appropriately
        UpdateRequest updateRequest = UpdateUtils.parse("Prefix ex: <http://example.org/> Insert Data { ex:s ex:p ex:o }");
        sparqlService
            .getUpdateExecutionFactory()
            .createUpdateProcessor(updateRequest)
            .execute();
    }

    public static void print(Collection<Binding> bindings) {
        for(Binding item : bindings) {
            System.out.println(item);
        }
    }

//    public static void processChanges(HashSetDiff<Binding> diff) {
//        System.out.println("Added:");
//        System.out.println("-----------------");
//        print(diff.getAdded());
//
//        System.out.println();
//        System.out.println("Removed");
//        System.out.println("-----------------");
//        print(diff.getRemoved());
//        //System.out.println(ResultSetFormatter.asText(diff.getRemoved()));
//    }
}
