package org.aksw.jena_sparql_api.update;

import java.util.Collection;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.commons.collections.diff.HashSetDiff;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.update.UpdateProcessor;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestUpdate {

    private Model model;
    private UpdateExecutionFactoryEventSource uef;
    private QueryExecutionFactory qef;

    private UpdateContext updateContext;

    @Before
    public void init() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("data/data-update-test.nt");

        model = ModelFactory.createDefaultModel();
        model.read(resource.getInputStream(), null, "N-TRIPLE");

        UpdateExecutionFactory tmp = new UpdateExecutionFactoryModel(model);
        qef = new QueryExecutionFactoryModel(model);

        updateContext = new UpdateContext(tmp, qef, 128, new QuadContainmentCheckerSimple()); //FunctionQuadDiffUnique.create(qef, )))

        uef = new UpdateExecutionFactoryEventSource(updateContext);
    }

    @Test
    public void test1() throws Exception
    {
        uef.getListeners().add(new DatasetListener() {

            @Override
            public void onPreModify(Diff<Set<Quad>> diff, UpdateContext updateContext) {
                System.out.println("Diff: " + diff);
            }
        });


        String requestStr = "Prefix owl: <http://www.w3.org/2002/07/owl#> Insert { ?s a owl:Thing } Where { ?s a <http://dbpedia.org/ontology/Person> }";
        UpdateProcessor processor = uef.createUpdateProcessor(UpdateUtils.parse(requestStr));
        processor.execute();

        //model.write(System.out, "N-TRIPLE");
    }

    @Test
    public void test2() throws Exception
    {
        test1();

        String requestStr = "Prefix owl: <http://www.w3.org/2002/07/owl#> Delete { ?s a owl:Thing } Where { ?s a <http://dbpedia.org/ontology/Person> }";
        UpdateProcessor processor = uef.createUpdateProcessor(UpdateUtils.parse(requestStr));
        processor.execute();
    }

    public static void print(Collection<Binding> bindings) {
        for(Binding item : bindings) {
            System.out.println(item);
        }
    }

    public static void processChanges(HashSetDiff<Binding> diff) {
        System.out.println("Added:");
        System.out.println("-----------------");
        print(diff.getAdded());

        System.out.println();
        System.out.println("Removed");
        System.out.println("-----------------");
        print(diff.getRemoved());
        //System.out.println(ResultSetFormatter.asText(diff.getRemoved()));
    }
}
