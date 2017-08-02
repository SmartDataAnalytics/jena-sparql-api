package org.aksw.jena_sparql_api.jgrapht;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.criteria.Root;
import javax.swing.JFrame;

import org.aksw.commons.util.strings.StringPrettyComparator;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndexJGraphT;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndexWrapper;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVar;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToGraphVisitor;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.aksw.jena_sparql_api.jgrapht.wrapper.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.mapper.jpa.core.RdfEntityManager;
import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.mapper.util.JpaUtils;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;


public class MainSparqlQueryToGraph {


    private static final Logger logger = LoggerFactory.getLogger(MainSparqlQueryToGraph.class);


    public static <K, V> Map<K, V> prettify(Map<? extends K, ? extends V> map) {
        Map<K, V> result = new TreeMap<>(StringPrettyComparator::doCompare);
        result.putAll(map);

        return result;
    }


    public static void main(String[] args) throws Exception {
        Iterable<Integer> iA = Arrays.asList(1, 2, 2, 3);
        Iterator<Integer> itA = iA.iterator();
        Stream<Integer> streamA = StreamSupport.stream(((Iterable<Integer>)() -> itA).spliterator(), false);
        Iterable<Integer> iB = () -> streamA.distinct().iterator();
        for(Integer item : iB) {
            System.out.println(item);
        }

        org.apache.jena.graph.Graph g = new GraphVarImpl(); //GraphFactory.createDefaultGraph();
        g.add(new Triple(Vars.s, Vars.p, Vars.o));
        RDFDataMgr.write(System.out, g, RDFFormat.NTRIPLES);
//        String[][] cases = {
//            { "Prefix : <http://ex.org/> Select * { ?a ?b ?c }",
//              "Prefix : <http://ex.org/> Select * { ?x ?y ?z }", },
//            { "Prefix : <http://ex.org/> Select * { ?d a ?f ; ?g ?h }",
//              "Prefix : <http://ex.org/> Select * { ?x a ?o ; ?y ?z }" },
//            { "Prefix : <http://ex.org/> Select * { ?i a :Bakery ; :locatedIn :Leipzig }",
//              "Prefix : <http://ex.org/> Select * { ?x a ?o . ?x ?y ?z }" },
//            { "Prefix : <http://ex.org/> Select * { ?x a ?o . ?x ?y ?z . ?z a ?w}" }
//        };

        String[][] cases = {
                { "Prefix : <http://ex.org/> Select * { ?a ?a ?a }",
                  "Prefix : <http://ex.org/> Select * { ?x ?x ?x . ?y ?y ?y }",
                  "Prefix : <http://ex.org/> Select * { ?x ?x ?x . ?y ?y ?y . ?z ?z ?z }" },

                { "Prefix : <http://ex.org/> Select * { ?as ?ap ?ao }",
                  "Prefix : <http://ex.org/> Select * { ?js ?jp ?jo . ?ks ?kp ?ko }",
                  "Prefix : <http://ex.org/> Select * { ?ss ?sp ?so . ?ts ?tp ?to .}" ,
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo . ?zs ?zp ?zo }"},

                // test examples with overlapping variables
                { "Prefix : <http://ex.org/> Select * { ?as ?ap ?ao }",
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo }",
                  "Prefix : <http://ex.org/> Select * { ?xs ?xp ?xo . ?ys ?yp ?yo . ?zs ?zp ?zo }" }

            };

        String caseA = cases[1][0];
        String caseB = cases[1][1];
        String caseC = cases[1][2];
        String caseD = cases[1][3];

        // This does not work with jgrapht due to lack of support for multi edges!!!

        Op aop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseA)));
        Op bop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseB)));
        Op cop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseC)));
        Op dop = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(caseD)));
        //System.out.println(op);


        aop = SparqlViewMatcherOpImpl.normalizeOp(aop);
        bop = SparqlViewMatcherOpImpl.normalizeOp(bop);
        cop = SparqlViewMatcherOpImpl.normalizeOp(cop);
        dop = SparqlViewMatcherOpImpl.normalizeOp(dop);


        //RDFDataMgr.write(System.out, graph, RDFFormat.NTRIPLES);

        Supplier<Supplier<Node>> ssn = () -> { int[] x = {0}; return () -> NodeFactory.createBlankNode("_" + x[0]++); };
        QueryToGraphVisitor av = new ExtendedQueryToGraphVisitor(ssn.get());
        aop.visit(av);
        GraphVar ag = av.getGraph();
        //System.out.println(ag.get);

        QueryToGraphVisitor bv = new ExtendedQueryToGraphVisitor(ssn.get());
        bop.visit(bv);
        GraphVar bg = bv.getGraph();

        QueryToGraphVisitor cv = new ExtendedQueryToGraphVisitor(ssn.get());
        cop.visit(cv);
        GraphVar cg = cv.getGraph();

        QueryToGraphVisitor dv = new ExtendedQueryToGraphVisitor(ssn.get());
        dop.visit(dv);
        GraphVar dg = dv.getGraph();

//        System.out.println("Graph A:");
//        RDFDataMgr.write(System.out, ag.getWrapped(), RDFFormat.NTRIPLES);
//        System.out.println(ag.getVarToNode());
//
//        System.out.println("Graph B:");
//        RDFDataMgr.write(System.out, bg.getWrapped(), RDFFormat.NTRIPLES);
//
        List<Map<Node, Node>> solutions = QueryToJenaGraph.match(HashBiMap.create(), bg, cg).collect(Collectors.toList());
//
//        System.out.println("VarMap entries: " + solutions.size());

        solutions.forEach(varMap -> {
            System.out.println(prettify(varMap));
        });


        SubGraphIsomorphismIndex<Node, Graph, Node> index =
                SubGraphIsomorphismIndexWrapper.wrap(
                        SubGraphIsomorphismIndexJGraphT.create(),//SubGraphIsomorphismIndexRdf.create();
                        PseudoGraphJenaGraph::new);
                        //kk -> SetOpsJGraphTRdfJena.INSTANCE.transformItems(new PseudoGraphJenaGraph(kk), v -> v));
        int xxx = 3;

        if(xxx == 0) {
            // incremental subsumtion
            index.add(ag);
            index.add(bg);
            index.add(cg);
        } else if(xxx == 2){
            // most generic inserted last
            index.add(dg);
            //index.add(cg);
            index.add(bg);
            index.add(ag);
        } else {
            SparqlEntityManagerFactory emf = new SparqlEntityManagerFactory();
            Model model = RDFDataMgr.loadModel("lsq-sparqlqc-synthetic-simple.ttl", Lang.TURTLE);
            SparqlService ss = FluentSparqlService.from(model).create();
            //SparqlService ss = FluentSparqlService.http("http://localhost:8950/sparql").create();

            emf.setSparqlService(ss);
            emf.addScanPackageName(MainSparqlQueryToGraph.class.getPackage().getName());
            RdfEntityManager em = emf.getObject();

            List<LsqQuery> queries;
            int yyy = 6;
            if(yyy == 5) {
                queries = JpaUtils.createTypedQuery(em, LsqQuery.class, (cb, cq) -> {
                    Root<LsqQuery> root = cq.from(LsqQuery.class);
                    cq.select(root);
                }).setMaxResults(1000).getResultList();


            } else if(yyy == 1) {
                queries = new ArrayList<>();
                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q-005cc91b"));
                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q-00061e2b"));
            } else if(yyy == 2) {
                queries = new ArrayList<>();
                //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q-01a34de1"));
                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q-03aa957f")); // SELECT  ?s ?str WHERE   { ?s  ?p  ?str     FILTER strstarts(str(?str), "1")   }
                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q-0000eb19")); // SELECT  ?property ?value WHERE   { <http://data.semanticweb.org/organization/a-i-group-faculty-of-technology-bielefeld-university-germany>               ?property  ?value   } "
                //the second query has the filter: SELECT  ?s ?str WHERE   { ?s  ?p  ?str     FILTER strstarts(str(?str), "1")   } "
                // TODO figure out why it is subsumed by ?s ?p ?o - and even more: why it subsumes every other query
                  //96 keys: [http://lsq.aksw.org/res/q-01a34de1 (SELECT ?s ?p ?o), http://lsq.aksw.org/res/q-011578c8 (ASK ?s ?p ?o)]
                    //    518 keys: [http://lsq.aksw.org/res/q-03aa957f]            }
                //       519 keys: [http://lsq.aksw.org/res/q-0000eb19]
            } else if(yyy == 6) {
                // Problematic orders:
                // - q3a, q2a, q1b, q1a
                queries = Lists.newArrayList();

                //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q4w"));
//                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q1x"));
//                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q3z"));
//                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q2y"));
                //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q1b"));

                //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q2y"));
                //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q1x"));
                //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q3z"));
                //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q4w"));

                // Test case: Multi-subsumption
//                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q1x"));
//                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q2y"));
//                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/r1a"));
//                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/spo"));


                // Test case: New root inserted late
                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q1x"));
                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q2y"));
                queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/spo"));

            } else {
                queries = Collections.emptyList();
            }
            //System.out.println(queries);
            int i = 0;


            queries = Lists.newArrayList(queries);
            for(LsqQuery q : queries) {
                String id = em.getIri(q);
                q.setIri(id);
            }

            //Collections.shuffle(queries);
            for(LsqQuery q : queries) {
                System.out.println("Item: " + q);
            }
            //Collections.sort(queries, (x, y) -> Objects.compare(x.getIri(), y.getIri(), Comparator.reverseOrder()));
            int iii = 0;
            for(LsqQuery lsqq : queries) {

                // TODO HACK We need to fetch the iri from the em, as the mapper currently does not support placing an entity's iri into a field

                System.out.println("Got lsq query: " + lsqq);

                String queryStr = lsqq.getText();
                Query query;
                try {
                    query = SparqlQueryParserImpl.create().apply(queryStr);
                } catch(Exception e) {
                    logger.warn("Failed to parse: " + queryStr);
                    continue;
                }
                Op op = Algebra.toQuadForm(Algebra.compile(query));
                Op nop = SparqlViewMatcherOpImpl.normalizeOp(op);


                // Collect all conjunctive queries
                if(!(nop instanceof OpExtConjunctiveQuery)) {
                    //System.out.println("Not a conjunctive query - skipping");
                } else {
                    ++i;
                    OpExtConjunctiveQuery ocq = (OpExtConjunctiveQuery)nop;
                    //ConjunctiveQuery cq = SparqlCacheUtils.tryExtractConjunctiveQuery(op, generator)

                    System.out.println("indexing: " + ocq.getQfpc());

                    QueryToGraphVisitor q2g = new ExtendedQueryToGraphVisitor(ssn.get());
                    q2g.visit(ocq);
                    GraphVar graph = q2g.getGraph();
                    //System.out.println(graph);

                    if(false) {
                    graph = new GraphVarImpl();
                    if(iii >= 0) { graph.add(new Triple(Vars.s.asNode(), RDF.type.asNode(), OWL.Class.asNode())); }
                    if(iii >= 1) { graph.add(new Triple(Vars.s.asNode(), RDFS.label.asNode(), Vars.l.asNode())); }
                    if(iii >= 2) { graph.add(new Triple(Vars.s.asNode(), FOAF.name.asNode(), Vars.o.asNode())); }
                    ++iii;
                    }

                    index.put(NodeFactory.createURI(lsqq.getIri()), graph);
                }



            }
            System.out.println("Processed: " + i);
index.printTree();


            System.exit(0);

        }

//        System.out.println("Performing lookup");
//        index.lookupFlat(cg).entries().forEach(e -> System.out.println("Lookup result: " + e.getKey() + ": " + prettify(e.getValue())));

        System.out.println("Index tree: ");
        index.printTree();

        Map<Node, Iterable<BiMap<Node, Node>>> map = index.lookupStream(cg, false);

        map.forEach((k, p) -> {
            System.out.println("Solutions for : " + k);
            //System.out.println("Estimated cost: " + p.getEstimatedCost());
            p.forEach(s -> {
                System.out.println("  " + s);
            });
            System.out.println("done");
        });



        //SparqlQueryContainmentUtils.match(viewQuery, userQuery, qfpcMatcher)
        org.jgrapht.Graph<?, ?> xg = new PseudoGraphJenaGraph(dg);
        //System.out.println(graph);
        if(false) {
            visualizeGraph(xg);
        }
    }


    public static void visualizeGraph(org.jgrapht.Graph<?, ?> graph) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 800);
        JGraph jgraph = new JGraph(new JGraphModelAdapter(graph));
        jgraph.setScale(1);
        final JGraphLayout hir = new JGraphHierarchicalLayout();
        // final JGraphLayout hir = new JGraphSelfOrganizingOrganicLayout();

        final JGraphFacade graphFacade = new JGraphFacade(jgraph);
        hir.run(graphFacade);
        final Map nestedMap = graphFacade.createNestedMap(true, true);
        jgraph.getGraphLayoutCache().edit(nestedMap);

        frame.getContentPane().add(jgraph);
        frame.setVisible(true);
    }

}
//// Do this state space search thingy: update the state, track the changes, compute and restore
//// This means: track which keys will be added, add them, and later remove them again
//boolean isCompatible = MapUtils.isCompatible(iso, baseIso);
//if(!isCompatible) {
//  writer.println("Not compatible with current mapping");
//  writer.incIndent();
//  writer.println("baseIso: " + baseIso);
//  writer.println("iso: " + iso);
//  writer.decIndent();
//  throw new RuntimeException("This should never happen - unless either there is a bug or even worse there is a conecptual issues");
//  //return;
//}
//
