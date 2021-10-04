package org.aksw.jena_sparql_api.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.entity.graph.metamodel.MainPlaygroundResourceMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.PredicateStats;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceState;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

/**
 * A class to retrieve the triples that correspond to a set of RDF resources w.r.t.
 * given schemas.
 *
 * @author raven
 *
 */
public class NodeSchemaDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(NodeSchemaDataFetcher.class);

    /**
     * Build the query that retrieves the information that matches the schemas for the given nodes.
     *
     * This method does not perform any optimizations:
     * If the same node occurs under multiple schemas with overlapping properties then
     * needless amounts of data are accessed.
     * In principle a preprocessing step could be applied that optimizes the 'schemaAndNodes' argument.
     * TODO Whenever we provide such a preprocessing implementation link to it from here
     *
     * @param schemaAndNodes
     * @return
     */
    public static Query toQuery(Multimap<NodeSchema, Node> schemaAndNodes) {

        Set<Query> unionMembers = new LinkedHashSet<>();
        for (Entry<NodeSchema, Collection<Node>> e : schemaAndNodes.asMap().entrySet()) {
            NodeSchema schema = e.getKey();
            Collection<Node> nodes = e.getValue();
            Query unionMember = immediateSchemaToSparql(schema);
            QueryUtils.injectFilter(unionMember, ExprUtils.oneOf(Vars.s, nodes));

            unionMembers.add(unionMember);
        }

        Query result = QueryUtils.unionConstruct(unionMembers);
        return result;
    }


    public void sync(
            // NodeGraphSchema schema,
            Multimap<NodeSchema, Node> schemaAndNodes,
            SparqlQueryConnection conn,
            LookupService<Node, ResourceMetamodel> metaDataService,
            ResourceCache graphToResourceState
            ) {

        Multimap<NodeSchema, Node> done = HashMultimap.create();

        Multimap<NodeSchema, Node> next = schemaAndNodes;
        while (!next.isEmpty()) {
            Multimap<NodeSchema, Node> tmp = step(next, conn, done, metaDataService, graphToResourceState);
            next = tmp;
        }


    }

    public Multimap<NodeSchema, Node> step(
            // NodeGraphSchema schema,
            Multimap<NodeSchema, Node> schemaAndNodes,
            SparqlQueryConnection conn,
            Multimap<NodeSchema, Node> done,
            LookupService<Node, ResourceMetamodel> metaDataService,
            ResourceCache resourceCache) {

        // The map for what to fetch in the next breadth
        Multimap<NodeSchema, Node> next = HashMultimap.create();

        // Graph graph = GraphFactory.createDefaultGraph();


        List<Node> allNodes = schemaAndNodes.values().stream().distinct().collect(Collectors.toList());
        Map<Node, ResourceMetamodel> metaData = metaDataService.fetchMap(allNodes);

        System.out.println("Showing " + metaData.size() + " metadata items:");
        metaData.entrySet().forEach(System.out::println);

        // Create a table of requested predicates and their target schemas
//        Table<Boolean, Node, Set<NodeSchema>> dirToPToTgts = HashBasedTable.create();
//
//        for (NodeSchema ns : schemaAndNodes.keySet()) {
//            for (PropertySchema ps : ns.getPredicateSchemas()) {
//                boolean isFwd = ps.isForward();
//                Node p = ps.getPredicate();
//
//                Set<NodeSchema> nss = dirToPToTgts.get(isFwd, p);
//                if (nss == null) {
//                    nss = new LinkedHashSet<>();
//                    dirToPToTgts.put(isFwd, p, nss);
//                }
//                nss.add(ns);
//            }
//        }

        // Map each source to the set of demanded properties according to the schemas
        // In the following this set gets reduced to the set of properties that need fetching
        // Properties that are already loaded or those having too many values are omitted
        Table<Boolean, Node, Set<Node>> dirToSrcToP = HashBasedTable.create();


        for (Entry<NodeSchema, Collection<Node>> e : schemaAndNodes.asMap().entrySet()) {
            NodeSchema ns = e.getKey();

            for (PropertySchema ps : ns.getPredicateSchemas()) {
                boolean isFwd = ps.isForward();
                Node p = ps.getPredicate();

                for (Node src : e.getValue()) {

                    Set<Node> preds = dirToSrcToP.get(isFwd, src);
                    if (preds == null) {
                        preds = new LinkedHashSet<>();
                        dirToSrcToP.put(isFwd, src, preds);
                    }
                    preds.add(p);
                }
            }
        }

        long valueCountThreshold = 10;


        Var ip = Var.alloc("ip");
        Var io = Var.alloc("io");

        Triple fwdTriplePattern = Triple.create(Vars.s, Vars.p, Vars.o);
        Triple bwdTriplePattern = Triple.create(io, ip, Vars.s);
        List<Triple> tps = new ArrayList<>(2);

        List<Element> tgtElts = new ArrayList<>();

        // Map each src to the predicates that need to be fetched for it
        // From that set remove blacklisted predicates and already loaded ones
        for (int i = 0; i < 2; ++i) {
            boolean isFwd = i == 0;
            Map<Node, Set<Node>> srcToPreds = dirToSrcToP.row(isFwd);

            for (Entry<Node, Set<Node>> e : srcToPreds.entrySet()) {
                Node src = e.getKey();
                Set<Node> preds = e.getValue();

                // Remove already loaded predicates
                ResourceState rs = resourceCache.getOrCreate(src);
                Set<Node> seenPreds = rs.getSeenPredicates(isFwd);
                preds.removeAll(seenPreds);

                Node g = Quad.defaultGraphIRI;

                ResourceMetamodel mm = metaData.get(src);
                // Node g = Node.ANY; // TODO Provide graph in some context
                Stream<PredicateStats> statsStream = mm == null ? null : mm.find(g, isFwd, Node.ANY);
                // We queried for the metamodel before so null stats should not happen!
                if (statsStream != null) {
                    List<PredicateStats> stats = statsStream.collect(Collectors.toList());
                    for (PredicateStats stat : stats) {
                        Node p = stat.getPredicateNode();
                        Long valueCount = stat.getValueCount();

                        if (valueCount != null && valueCount >= valueCountThreshold) {
                            preds.remove(p);
                        }
                    }
                }
            }


            // We now know for each src the effective predicates
            // Group srcs by predicates
            Multimap<Set<Node>, Node> predsToSrcs = Multimaps.invertFrom(Multimaps.forMap(srcToPreds), HashMultimap.create());

            if (!predsToSrcs.isEmpty()) {

                Triple t = isFwd ? fwdTriplePattern : bwdTriplePattern;
                tps.add(t);
                Var srcVar = (Var)t.getSubject();
                Element tp = ElementUtils.createElementTriple(t);


                for (Entry<Set<Node>, Collection<Node>> e : predsToSrcs.asMap().entrySet()) {
                    Collection<Node> preds = e.getKey();
                    Collection<Node> srcs = e.getValue();

                    Element elt = ElementUtils.createElementGroup(
                        tp,
                        new ElementFilter(ExprUtils.oneOf(srcVar, srcs)),
                        new ElementFilter(ExprUtils.oneOf(Vars.p, preds)));

                    tgtElts.add(elt);
                }
            }
        }

        Query unionQuery = new Query();
        // unionQuery.setQueryConstructType();
        unionQuery.setQuerySelectType();
        unionQuery.setConstructTemplate(new Template(BasicPattern.wrap(tps)));
        unionQuery.setQueryPattern(ElementUtils.unionIfNeeded(tgtElts));





        // Query unionQuery = toQuery(schemaAndNodes);
        logger.debug("Union Query: " + unionQuery);
        System.out.println(unionQuery);

        // SparqlRx.execConstructTriples(conn, unionQuery).forEach(graph::add);
        SparqlRx.execSelectRaw(conn, unionQuery).forEach(b -> {
            Node src = b.get(Vars.s);
            Node np = b.get(Vars.p);
            Node no = b.get(Vars.o);
            Node nip = b.get(ip);
            Node nio = b.get(io);

            boolean isFwd = np != null;
            Node p = isFwd ? np : nip;
            Node o = isFwd ? no : nio;

            ResourceState rs = resourceCache.get(src);
            rs.add(isFwd, p, o);
        });

        // Declare all predicates for which we performed the retrieval as seen
        // even (or especially) if there were no results for them
        for (Cell<Boolean, Node, Set<Node>> cell : dirToSrcToP.cellSet()) {
            boolean isFwd = cell.getRowKey();
            Node src = cell.getColumnKey();
            Set<Node> preds = cell.getValue();

            ResourceState rs = resourceCache.get(src);
            for (Node pred : preds) {
                System.out.println("Declaring seen: " + pred);
                rs.declarePredicateSeen(isFwd, pred);
            }
        }

        // Set up the next iteration

        for (Entry<NodeSchema, Collection<Node>> e : schemaAndNodes.asMap().entrySet()) {
            NodeSchema ns = e.getKey();

            for (PropertySchema ps : ns.getPredicateSchemas()) {
                boolean isFwd = ps.isForward();
                Node p = ps.getPredicate();

                Set<? extends NodeSchema> targetSchemas = ps.getTargetSchemas();
                if (targetSchemas != null) {
                    for (NodeSchema targetSchema : targetSchemas) {


                        for (Node src : e.getValue()) {
                            ResourceState rs = resourceCache.get(src);
                            Set<Node> targets = rs.getTargets(isFwd, p);
                            if (targets != null) {
                                for (Node targetNode : targets) {
                                    if (!done.containsEntry(targetSchema, targetNode)) {
                                        done.put(targetSchema, targetNode);
                                        next.put(targetSchema, targetNode);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        return next;
    }


    public static Query immediateSchemaToSparql(NodeSchema schema) {
        Set<Expr> fwdDisjunction = new LinkedHashSet<>();
        Set<Expr> bwdDisjunction = new LinkedHashSet<>();
        for (PropertySchema predicateSchema : schema.getPredicateSchemas()) {
            boolean isFwd = predicateSchema.isForward();

            Node p = predicateSchema.getPredicate();
            Expr expr = new E_Equals(new ExprVar(Vars.p), NodeValue.makeNode(p));
//            ExprList el = new ExprList(expr);

            if (isFwd) {
                fwdDisjunction.add(expr);
            } else {
                bwdDisjunction.add(expr);
            }
        }

        for (DirectedFilteredTriplePattern dftp : schema.getGenericPatterns()) {
            // Align the variable naming
            // Map<Var, Var> var

            // TernaryRelation tr = new TernaryRelationImpl(new ElementFilter(), dftp.getTriplePattern())
        }

        // inverse predicate / object variables
        Var ip = Var.alloc("ip");
        Var io = Var.alloc("io");

        Triple fwdTriplePattern = Triple.create(Vars.s, Vars.p, Vars.o);
        Triple bwdTriplePattern = Triple.create(io, ip, Vars.s);

        List<Triple> tps = new ArrayList<>(2);
        List<Element> elts = new ArrayList<>(2);
        if (!fwdDisjunction.isEmpty()) {
            tps.add(fwdTriplePattern);
            elts.add(
                ElementUtils.groupIfNeeded(
                    ElementUtils.createElement(fwdTriplePattern),
                    new ElementFilter(ExprUtils.orifyBalanced(fwdDisjunction))
                ));
        }

        if (!bwdDisjunction.isEmpty()) {
            tps.add(bwdTriplePattern);
            elts.add(
                    ElementUtils.groupIfNeeded(
                        ElementUtils.createElement(bwdTriplePattern),
                        new ElementFilter(ExprUtils.orifyBalanced(bwdDisjunction))
                    ));
        }


        Query stdQuery = new Query();
        stdQuery.setQueryConstructType();
        stdQuery.setConstructTemplate(new Template(BasicPattern.wrap(tps)));
        stdQuery.setQueryPattern(ElementUtils.unionIfNeeded(elts));

        // AttributeGraphFragment result = new AttributeGraphFragment().addMandatoryJoin(Vars.s, stdQuery);
//        GraphPartitionJoin result = new GraphPartitionJoin(EntityGraphFragment.fromQuery(Vars.s, stdQuery));
//System.out.println(stdQuery);

        // return result;
        return stdQuery;
        //ListServiceEntityQuery ls = new ListServiceEntityQuery(conn, agf);
    }


    public static void main(String [] args) {
        MainPlaygroundResourceMetamodel.init();
//        NodeSchema schema = new NodeSchemaImpl();
//        PropertySchema pgs = schema.createPropertySchema(RDF.type.asNode(), true);
//        PropertySchema pgs2 = schema.createPropertySchema(DCTerms.identifier.asNode(), true);
//        PropertySchema pgs3 = schema.createPropertySchema(DCAT.distribution.asNode(), true);
//
//
//        NodeSchema schema2 = new NodeSchemaImpl();
//        schema2.createPropertySchema(DCAT.downloadURL.asNode(), true);
//        // pgs3.getTargetSchemas().add(schema2);


        Model shaclModel = RDFDataMgr.loadModel("dcat-ap_2.0.0_shacl_shapes.ttl");
        NodeSchema schema = shaclModel.createResource("http://data.europa.eu/r5r#Dataset_Shape").as(NodeSchemaFromNodeShape.class);


        Node datasetNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");

        Multimap<NodeSchema, Node> roots = HashMultimap.create();
        roots.put(schema, datasetNode);

        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");

//        RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);


        RDFConnection conn = RDFConnectionFactory.connect(ds);

        LookupService<Node, ResourceMetamodel> metaDataService = ResourceExplorer.createMetamodelLookup(conn);

        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
        // Map<Node, ResourceState> resourceCache = new HashMap<>();
        ResourceCache resourceCache = new ResourceCache();
        dataFetcher.sync(roots, conn, metaDataService, resourceCache);


        Graph graph = GraphFactory.createDefaultGraph();

        resourceCache.getMap().values().stream().flatMap(ResourceState::streamCachedTriples)
            .forEach(graph::add);



        // Traverser.forGraph(s -> resourceCache.get(s).find )

         Model m = ModelFactory.createModelForGraph(graph);
         RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);

         ShapedNode sn = ShapedNode.create(datasetNode, schema, resourceCache, conn);

         ShapedProperty sp = sn.getShapedProperties().get(PathFactory.pathLink(DCAT.distribution.asNode()));

         System.out.println("Is in memory: " + sp.isInMemory());
         // System.out.println("Value: " + sp.getValues().fetchData(null, RangeUtils.rangeStartingWithZero));
         // sp.getValues();

         printShapedNode(sn);
    }

    public static void printShapedNode(ShapedNode sn) {
        System.out.println("Visisted: " + sn.getSourceNode());
        Map<Path, ShapedProperty> spm = sn.getShapedProperties();

        for (ShapedProperty sp : spm.values()) {
            Long cnt = sp.getValues().fetchCount().blockingGet();
            if (cnt != null) {

                System.out.println("Path: " + sp.getPath());
                System.out.println("Is in memory: " + sp.isInMemory());
                System.out.println("Is empty: " + sp.isEmpty());
                System.out.println(cnt);

                Map<Node, ShapedNode> map = sp.getValues().fetchData(null, Range.closedOpen(0l, 3l));
                for (ShapedNode tgt : map.values()) {
                    printShapedNode(tgt);
                }

            }
        }
    }

}
