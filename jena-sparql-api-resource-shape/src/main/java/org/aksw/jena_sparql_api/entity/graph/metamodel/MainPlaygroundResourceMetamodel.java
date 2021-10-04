package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.collection.observable.ObservableGraph;
import org.aksw.jena_sparql_api.collection.observable.ObservableGraphImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.mapper.proxy.MapperProxyUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jena_sparql_api.schema.ResourceExplorer;
import org.aksw.jena_sparql_api.schema.SHAnnotatedClass;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DCAT;
import org.topbraid.shacl.model.SHFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import io.reactivex.rxjava3.core.Flowable;


/*

SELECT * {

SERVICE <https://dbpedia.org/sparql> { {

SELECT ?src ?p ?fwd ?g ?dvc {
   { SELECT ?src ?p ?g (COUNT(DISTINCT ?outTgt) AS ?dvc) (true AS ?fwd) {
     GRAPH ?g { ?src ?p ?outTgt
       # FILTER(?src = <http://dbpedia.org/resource/Leipzig>)
       FILTER(?src = <http://dbpedia.org/ontology/Person>)
     }
   } GROUP BY ?src ?p ?g }
   UNION
   { SELECT ?src ?p ?g (COUNT(DISTINCT ?inTgt) AS ?dvc) (false AS ?fwd) {
     GRAPH ?g { ?inTgt ?p ?src
       # FILTER(?src = <http://dbpedia.org/resource/Leipzig>)
       FILTER(?src = <http://dbpedia.org/ontology/Person>)
     }
   } GROUP BY ?src ?p ?g }
 }

} }
}
*/

/**
 * Resource Metamodel generation:
 *
 * - For a resource retrieve the number of out/ingoing properties and their number of distinct values per graph
 * The stats are used to decide which set of properties to fetch eagerly and which ones to process lazily.
 *
 * Requesting the values of a lazy property always creates new requests.
 * For eager properties all values are prefetched - so iteration of the values can operate from memory.
 *
 *
 *
 *
 * Ideas for improved robustness for Resource Metamodel generation:
 *
 * Given a set of resources:
 * - Thresholded count over the total number of triples:
 *     <pre>
 *     SELECT ?s ?inCount ?outCount {
 *       { SELECT ?s (COUNT(*) AS ?outCount) {
 *         SELECT * { ?s ?op ?oo FILTER(?s = <http://dbpedia.org/resource/Leipzig>) } LIMIT 1000 } }
 *       { SELECT ?s (COUNT(*) AS ?inCount) {
 *         SELECT * { ?io ?ip ?s FILTER(?s = <http://dbpedia.org/resource/Leipzig>) } LIMIT 1000 } }
 *     }
 *     </pre>
 * - If the thresholds are not exceeded then all triples can be fetched
 * - Otherwise: One or more predicates have a high number of values
 *     (e.g. a class with rdf:type in inversion direction may be linked to billions of instances)
 *     Try whether fetching the distinct predicates works (the db may have an index for that)
 *     If that fails then there is probably no easy / certain way to get all properties.
 *     Maybe we could try to fetch distinct properties within a chuck and then
 *     do repeated requests with those already seen properties excluded (only works if the db can skip index entries)
 *
 */

public class MainPlaygroundResourceMetamodel {

    public static void init() {
        JenaSystem.init();
        SHFactory.ensureInited();

        JenaPluginUtils.registerResourceClasses(
                NodeSchemaFromNodeShape.class,
                PropertySchemaFromPropertyShape.class,
                SHAnnotatedClass.class,

                ResourceMetamodel.class,
                ResourceGraphMetamodel.class,
                ResourceGraphPropertyMetamodel.class,
                PredicateStats.class,
                GraphPredicateStats.class,
                RGDMetamodel.class,

                ClassRelationModel.class,
                DatasetMetamodel.class
                );
    }

    public static void main(String[] args) {
        init();


        ConceptManager cm = new ConceptManagerImpl();
        Node name = cm.getOrCreate(Concept.parse("?s { ?s a <Person> }"));

        Node name2 = cm.getOrCreate(Concept.parse("?s { ?s a <Person> }"));



        System.out.println(name);
        System.out.println(name2);



        // testAnalyzeResources();
        createMetaModelQuery();
    }

//    public static void testCreateModel() {
//        DatasetMetamodel dsm = ModelFactory.createDefaultModel().createResource().as(DatasetMetamodel.class);
//        ResourceGraphMetamodel m = dsm.getOrCreateResourceMetamodel("urn:my-resource-1");
//
//        m.getKnownIngoingPredicateIris().add("http://foo.bar/baz");
//
//        PredicateStats ps = m.getOrCreateOutgoingPredicateStats("urn:my-predicate-1");
//        GraphPredicateStats gps1 = ps.getOrCreateStats("urn:my-graph-1");
//        gps1.setDistinctValueCount(123l);
//        gps1.setDistinctValueCountMinimum(true);
//
//        GraphPredicateStats gps2 = ps.getOrCreateStats("urn:my-graph-2");
//        gps2.setDistinctValueCount(123l);
//
//        m.getOrCreateOutgoingPredicateStats("urn:my-predicate-1")
//            .getOrCreateStats("urn:my-graph-1")
//            .setDistinctValueCount(666l);
//
//
//        RDFDataMgr.write(System.out, m.getModel(), RDFFormat.TURTLE_PRETTY);
//    }

    public static Flowable<ResourceMetamodel> computeCriticalMetamodel(SparqlQueryConnection conn, List<Node> nodes) {
//        Query query = SparqlStmtMgr.loadQuery("resource-criticalmodel.rq");
//        QueryUtils.injectFilter(query, ExprUtils.oneOf("src", nodes));
//
//
//        System.out.println(query);
//
//        Model m;
//        try (QueryExecution qe = conn.query(query)) {
//            m = qe.execConstruct();
//        }
//
//        // RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
//
//        Property p = ResourceFactory.createProperty("http://www.example.org/targetResource");
//        Set<ResourceMetamodel> tmp = m.listSubjectsWithProperty(p).mapWith(r -> r.as(ResourceMetamodel.class)).toSet();
//

        LookupService<Node, ResourceMetamodel> lookupService = ResourceExplorer.createMetamodelLookup(conn);
        List<ResourceMetamodel> tmp = lookupService.fetchList(nodes);


        System.out.println("Results:");
        for (ResourceMetamodel r : tmp) {
            System.out.println("Metamodel for: " +  r.getTargetResource());

            Map<Node, ResourceGraphMetamodel> rgm = r.byGraph();
            // System.out.println(rgm);

            for (Entry<Node, ResourceGraphMetamodel> e : rgm.entrySet()) {
                System.out.println("  Graph: " + e.getKey());

                ResourceGraphMetamodel rg = e.getValue();

                for (int i = 0; i < 2; ++i) {
                    boolean isFwd = i == 0;
                    System.out.println("    isFwd: " + isFwd);
                    RGDMetamodel rgd = rg.getStats(isFwd);

                    if (rgd != null) {
                        Map<Node, PredicateStats> rgpm = rgd.getPredicateStats();

                        // System.out.println(rgp);

                        for (Entry<Node, PredicateStats> f : rgpm.entrySet()) {
                            System.out.println("      " + f.getKey());
                            System.out.println("      " + f.getValue().getValueCount());
                        }
                    }
                }


            }
        }


        for (ResourceMetamodel x : tmp) {
            HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(x);

            // The mapping of RDFNodes to string IDs (not IRIs at this point) is obtained via
            Map<RDFNode, String> renames = hashIdCxt.getStringIdMapping();

            // Get a mapping from the original resources to the renamed ones.
            ResourceUtils.renameResources("http://www.example.org/", renames);

        }

//
//        System.out.println("SKOLEMIZED:");
//        RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
//
//        tmp = m.listSubjectsWithProperty(p).mapWith(r -> r.as(ResourceMetamodel.class)).toSet();


        Flowable<ResourceMetamodel> result = Flowable.fromIterable(tmp);

        return result;
    }

    public static void createMetaModelQuery() {

        // RDFConnection conn = RDFConnectionFactory.connect("https://dbpedia.org/sparql");
        SparqlQueryConnection conn = new SparqlQueryConnectionJsa(FluentQueryExecutionFactory.http("https://dbpedia.org/sparql")
            .config().withClientSideConstruct().end().create());


        computeCriticalMetamodel(conn, Arrays.asList(NodeFactory.createURI("http://dbpedia.org/resource/Leipzig")));

        /*
        Query query = SparqlStmtMgr.loadQuery("resource-criticalmodel.rq");

        // Concept c = Concept.createIris("src", "http://dbpedia.org/resource/Leipzig>");

        QueryUtils.injectFilter(query, ExprUtils.oneOfIris("src", "http://dbpedia.org/resource/Leipzig"));

        System.out.println(query);

        Model m;
        Dataset ds = DatasetFactory.create();
        try (QueryExecution qe = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", query)) {
            m = qe.execConstruct();
        }*/

        // RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_BLOCKS);

    }


    public static void testAnalyzeResources() {
         DatasetMetamodel dsm = ModelFactory.createDefaultModel().createResource().as(DatasetMetamodel.class);


        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RDFConnection conn = RDFConnectionFactory.connect(ds);

        ObservableGraph shapeGraph = ObservableGraphImpl.decorate(RDFDataMgr.loadGraph("dcat-ap_2.0.0_shacl_shapes.ttl"));
        shapeGraph.addPropertyChangeListener(ev -> System.out.println("Event: " + ev));
        Model shapeModel = ModelFactory.createModelForGraph(shapeGraph);
        NodeSchemaFromNodeShape ns = shapeModel.createResource(DCAT.Dataset.getURI()).as(NodeSchemaFromNodeShape.class);

        Multimap<Node, NodeSchema> roots = ArrayListMultimap.create();

        Node sourceNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");

        NodeSchema userSchema = ModelFactory.createDefaultModel().createResource().as(NodeSchemaFromNodeShape.class);
        roots.put(sourceNode, userSchema);


        // NodeSchema baseSchema = new NodeSchemaFromNodeShape(ns);
        roots.put(sourceNode, ns);

        Multimap<NodeSchema, Node> schemaToNodes = HashMultimap.create();
        Multimaps.invertFrom(roots, schemaToNodes);

        analyzeResources(dsm, schemaToNodes, shapeModel, conn);

        RDFDataMgr.write(System.out, dsm.getModel(), RDFFormat.TURTLE_PRETTY);
    }

    public static void analyzeResources(
            DatasetMetamodel dsm,
            Multimap<NodeSchema, Node> schemaToNodes,
            Model shapeModel,
            RDFConnection conn) {


        // Try to fetch for every resource all triples w.r.t. to the schema
        // If that fails then first request the counts for every predicate
        // - only prefetch information if the count is sufficiently low


        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();

        LookupService<Node, ResourceMetamodel> metaModelService =
                ResourceExplorer.createMetamodelLookup(conn).cache();


        //Graph dataGraph = GraphFactory.createDefaultGraph();
        Map<Node, ResourceState> resourceCache = new HashMap<>();
        // dataFetcher.sync(schemaToNodes, conn, metaModelService, resourceCache);

        // fillMetamodel(dsm, schemaToNodes, dataGraph);



    }

    public static void fillMetamodel(DatasetMetamodel dsm, Multimap<NodeSchema, Node> schemaToNodes, Graph dataGraph) {
        for (Entry<NodeSchema, Collection<Node>> e : schemaToNodes.asMap().entrySet()) {
            NodeSchema nodeSchema = e.getKey();
            for (Node node : e.getValue()) {
                fillModel(dsm, dataGraph, nodeSchema, node);
            }
        }
    }

    public static void fillModel(DatasetMetamodel dsm, Graph dataGraph,
            NodeSchema nodeSchema, Node node) {
        ResourceGraphMetamodel rmm = dsm.getOrCreateResourceMetamodel(node);
        // rmm.getOutgoingPredicateStats().get(Node.ANY).getGraphToPredicateStats().get(Node.ANY).get
        for (PropertySchema propertySchema : nodeSchema.getPredicateSchemas()) {
            boolean isForward = propertySchema.isForward();

            Set<? extends NodeSchema> targetSchemas = propertySchema.getTargetSchemas();
            if (targetSchemas != null) {
                for(NodeSchema targetSchema : targetSchemas) {

                    Iterator<Triple> it = propertySchema.streamMatchingTriples(node, dataGraph).iterator();
                    while (it.hasNext()) {
                        Triple triple = it.next();
                        Node p = triple.getPredicate();
        //                        Node source = TripleUtils.getSource(triple, isForward);

                        // rmm.getKnownPredicates(isForward).add(p);

                        // We don't have stats here

        //                        GraphPredicateStats stats = rmm
        //                                .getOrCreatePredicateStats(node, isFoward)
        //                                .getOrCreateStats(Quad.defaultGraphNodeGenerated);
                        Node targetNode = TripleUtils.getTarget(triple, isForward);

                        if (targetSchema != null) {
                            fillModel(dsm, dataGraph, targetSchema, targetNode);
                        }
                    }
                }
            }
        }
    }

}
