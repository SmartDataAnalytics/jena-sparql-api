package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Assert;
import org.junit.Test;

/**
 * Benchmark to compare Map views backed by an RDF graph
 * with a native Java HashMap. Naturally, the Java map is expected to be significantly faster,
 * however the purpose of this class is to quantify the difference in performance.
 *
 * Results on 2021-06-20 on xps 13 9360 in warm state:
 *    JavaMap Ops/Sec (by iterations): 2960170.0271932767
 *    RdfMap Ops/Sec (by iterations):    74452.90704329098
 * Native java maps are roughly 50 times faster than going through the RDF layer
 * via the mapper-proxy module.
 *
 *
 *
 * @author raven
 *
 */
public class TestRdfMapPerformance {

    public static final int MAX_KEY = 1000000;

    @ResourceView
    interface RMap extends Resource{
        @Iri("urn:map")
        Map<Integer, Integer> getMap();
    }

    @Test
    public void benchmarkMapOpsPerSecondsByTimeLimit() {
        JenaPluginUtils.registerResourceClasses(RMap.class);

        for (int i = 0; i < 3; ++i) {
            Map<Integer, Integer> javaMap = new HashMap<>();
            {
                Random rand = new Random(0);
                System.err.println("JavaMap Ops/Sec (by time limit): " + BenchmarkUtils.benchmarkOpsPerSecByTimeLimit(() -> javaMap.put(rand.nextInt(MAX_KEY), rand.nextInt(MAX_KEY))));
            }

            Map<Integer, Integer> rdfMap = ModelFactory.createDefaultModel().createResource().as(RMap.class).getMap();
            {
                Random rand = new Random(0);
                System.err.println("RdfMap Ops/Sec (by time limit): " + BenchmarkUtils.benchmarkOpsPerSecByTimeLimit(() -> rdfMap.put(rand.nextInt(MAX_KEY), rand.nextInt(MAX_KEY))));
            }
        }
    }

    @Test
    public void benchmarkMapOpsPerSecByIterations() {
        JenaPluginUtils.registerResourceClasses(RMap.class);

        int numIterations = 100000;
        for (int i = 0; i < 3; ++i) {
            Map<Integer, Integer> javaMap = new HashMap<>();
            {
                Random rand = new Random(0);
                System.err.println("JavaMap Ops/Sec (by iterations): " + BenchmarkUtils.benchmarkOpsPerSecByIterations(numIterations, () -> javaMap.put(rand.nextInt(MAX_KEY), rand.nextInt(MAX_KEY))));
            }

            Map<Integer, Integer> rdfMap = ModelFactory.createDefaultModel().createResource().as(RMap.class).getMap();
            {
                Random rand = new Random(0);
                System.err.println("RdfMap Ops/Sec (by iterations): " + BenchmarkUtils.benchmarkOpsPerSecByIterations(numIterations, () -> rdfMap.put(rand.nextInt(MAX_KEY), rand.nextInt(MAX_KEY))));
            }

            Assert.assertEquals(javaMap, rdfMap);
        }
    }

}
