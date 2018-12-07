package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.ElementTreeAnalyser;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.ReplaceConstants;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.expr.NodeValueUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathConstraint3 {

    private static final Logger logger = LoggerFactory.getLogger(PathConstraint3.class);

    public static void getPathConstraints(Concept concept) {

        Query query = concept.asQuery();

        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);

        op = ReplaceConstants.replace(op);

        /*
        ExprList exprs = FilterUtils.collectExprs(op, new ExprList());
        Collection<Quad> quads =  PatternUtils.collectQuads(op, new ArrayList<Quad>());


        List<ExprList> clauses = DnfUtils.toClauses(exprs);
        System.out.println("DNF = " + clauses);

        Set<Set<Expr>> dnf = FilterUtils.toSets(clauses);

        System.out.println("aaoeuaoeutsh");
        */
    }

    public static List<Quad> collectQuads(Element element) {
        List<Quad> result = new ArrayList<Quad>();
        Context context = new Context();

        collectQuads(element, context, result);

        return result;
    }


    public static void collectQuads(Element element, Context context, List<Quad> result) {
        if(element instanceof ElementTriplesBlock) {
            collectQuads((ElementTriplesBlock)element, context, result);
        }
        else if(element instanceof ElementGroup) {
            collectQuads((ElementGroup)element, context, result);
        }
        else if(element instanceof ElementPathBlock) {
            collectQuads((ElementPathBlock)element, context, result);
        }
        else {
            logger.warn("Omitting unsupported element type: " + element.getClass() + " - " + element);
        }
    }

    public static void collectQuads(ElementTriplesBlock element, Context context, List<Quad> result) {
        Node graphNode = context.getGraphNode();

        for(Triple triple : element.getPattern().getList()) {
            Quad quad = new Quad(graphNode, triple);
            result.add(quad);
        }
    }

    public static void collectQuads(ElementGroup element, Context context, List<Quad> result) {
        for(Element e : element.getElements()) {
            collectQuads(e, context, result);
        }
    }

    public static void collectQuads(ElementPathBlock element, Context context, List<Quad> result) {
        Node graphNode = context.getGraphNode();

        for(TriplePath triplePath : element.getPattern().getList()) {
            Triple triple = triplePath.asTriple();

            if(triple == null) {
                logger.warn("Omitted non-simple triple");
            }

            Quad quad = new Quad(graphNode, triple);
            result.add(quad);
        }
    }


    public static void getPathConstraints(OpProject op) {

    }

    public static void getPathConstraints(OpFilter op) {
        ExprList exprs = op.getExprs();



    }

    public static void getPathConstraints(OpQuadPattern op) {

    }

//    public static final String varNs = "http://dummy.org/var/";
//
//    public static Var uriToVar(Node node) {
//        Var result = null;
//
//        if(node.isVariable()) {
//            result = (Var)node;
//        }
//        else if(node.isURI()) {
//            String tmp = node.getURI();
//            if(tmp.startsWith(varNs)) {
//                String suffix = tmp.substring(varNs.length());
//
//                result = Var.alloc(suffix);
//            }
//        }
//
//        return result;
//    }
//
//    public static Node createVarUri(Var var) {
//        return NodeFactory.createURI(varNs + var.getName());
//    }

//    public static Quad createUriVars(Quad quad) {
//
//        Node g = quad.getGraph();
//        Node s = quad.getSubject();
//        Node p = quad.getPredicate();
//        Node o = quad.getObject();
//
//        if(g.isVariable()) { g = createVarUri((Var)g); }
//        if(s.isVariable()) { s = createVarUri((Var)s); }
//        // Leave predicate untouched
//        if(o.isVariable()) { o = createVarUri((Var)o); }
//
//
//        Quad result = new Quad(g, s, p, o);
//        return result;
//    }

    public static Concept getPathConstraintsSimple(UnaryRelation concept) {
        //Model model = ModelFactory.createDefaultModel();


        ElementTreeAnalyser analyser = new ElementTreeAnalyser(concept.getElement());
        List<Quad> quads = analyser.getQuads();


        Set<Var> predVars = new HashSet<Var>();
        for(Quad quad : quads) {
            Node pred = quad.getPredicate();
            if(pred.isVariable()) {
                Var v = (Var)pred;

                predVars.add(v);
            }
        }

        List<Expr> exprs = analyser.getFilterExprs();
        Expr expr = ExprUtils.andifyBalanced(exprs);


        Expr cnf = CnfUtils.eval(expr);
        List<ExprList> clauses = CnfUtils.toClauses(cnf);


        ExprList predExprOrs = new ExprList();
        if(clauses != null) {
            for(ExprList clause : clauses) {
                Set<Var> clauseVars = clause.getVarsMentioned();
                if(predVars.containsAll(clauseVars)) {
                    Expr or = ExprUtils.orifyBalanced(clause);
                    predExprOrs.add(or);
                }
            }
        }
        Expr predExpr = ExprUtils.andifyBalanced(predExprOrs);


        //List<Quad> quads = collectQuads(concept.getElement());

        Graph model = new GraphVarImpl();
        
        for(Quad quad : quads) {
            // Replace variables with fake uris
            //Quad q = createUriVars(quad);

                        //if(!q.getPredicate().isVariable()) {

            model.add(quad.asTriple());
                //Statement stmt = model.asStatement(q.asTriple());
                //model.add(stmt);
            //}
        }


        //Node s = createVarUri(concept.getVar());
        //Resource start = model.asRDFNode(s).asResource();

        Set<Triple> result = new LinkedHashSet<Triple>();
        
        Map<Node, Var> map = new LinkedHashMap<>();
        Generator<Var> generator = VarGeneratorBlacklist.create(concept.getVarsMentioned());
        Function<Node, Var> nodeToVar = n -> map.computeIfAbsent(n, k -> generator.next());
        
        // Derive enumerations from rdf:type
        Set<Node> types = model.find(concept.getVar(), RDF.type.asNode(), Node.ANY).mapWith(Triple::getObject).toSet();
        if(!types.isEmpty()) {
        	Expr ex = ExprUtils.oneOf(concept.getVar(), types);
        	predExpr = predExpr == null ? ex : new E_LogicalAnd(predExpr, ex);
        }
        
        //        for(Node type : types) {                	
//            Triple u = new Triple(p, VocabPath.isIngoingPredicateOf.asNode(), type);
//            result.add(u);
//        }

        
        
        createQueryForward(model, concept.getVar(), nodeToVar, result);
        //createQueryBackward(model, concept.getVar(), , result);

        if(result.isEmpty()) {
        	// Create a concept ?s | ?s ?p ?o - where ?s = concept.getVar() and the other variables properly renaming 
        	UnaryRelation tmp = ConceptUtils.createSubjectConcept();
        	Triple tr = ElementUtils.extractTriple(
        			new Concept(new ElementGroup(), concept.getVar())
        			.prependOn(concept.getVar())
        			.with(tmp)
        			.getElement());
        	result.add(tr);
        }
        
        BasicPattern bgp = BasicPattern.wrap(new ArrayList<Triple>(result));
        ElementTriplesBlock triplesBlock = new ElementTriplesBlock(bgp);

        Element element;


        if(predExpr == null) {
            element = triplesBlock;
        } else {
            Expr e = ExprUtils.andifyBalanced(predExpr);

            ElementFilter filter = new ElementFilter(e);
            ElementGroup group = new ElementGroup();
            group.addElement(triplesBlock);
            group.addElement(filter);

            element = group;
        }



        Concept c = new Concept(element, concept.getVar());


        logger.debug("Path query is: " + c);

        return c;
    }

    
    public static void createQuery(Graph graph, Node src, Set<Triple> result) {
    	
    }

    public static void createQueryForward(Graph graph, Node src, Function<Node, Var> nodeToVar, Set<Triple> result) {
        Set<Triple> succs = graph.find(src, Node.ANY, Node.ANY).toSet();

        
        for(Triple stmt : succs) {
            Node p = stmt.getPredicate();
            Node o = stmt.getObject();
//            if(o.isLiteral()) {
//                continue;
//            }
            
            // TODO Make the forbidden list configurable
            if(RDF.type.asNode().equals(p)) {
            	continue;
            }


            Node s = src.isVariable() || src.isBlank() ? src : nodeToVar.apply(src);
            // Check if we know something about the type constraint of src
            Set<Node> types = graph.find(o, RDF.type.asNode(), Node.ANY).mapWith(Triple::getObject).toSet();
            for(Node type : types) {                	
                Triple u = new Triple(s, p, type);
                if(!result.contains(u)) {
                	result.add(u);
                	createQueryForward(graph, o, nodeToVar, result);
                }
            }

            
//            Triple t = new Triple(s, VocabPath.hasOutgoingPredicate.asNode(), p);
//            if(!result.contains(t)) {
//                result.add(t);
//                
//
//                createQueryForward(graph, o, nodeToVar, result);
//            }
        }
    }
//
//    public static void createQueryBackward(Model model, Node node, Resource res, Set<Triple> result) {
//        Node n;
//        if(node.isVariable()) {
//            n = createVarUri((Var)node);
//        } else {
//            n = node;
//        }
//
//        Resource r = model.asRDFNode(n).asResource();
//
//        Set<Statement> preds = model.listStatements(null, null, r).toSet();
//        for(Statement stmt : preds) {
//            Resource s = stmt.getSubject();
//            Property p = stmt.getPredicate();
//
//            //Triple t = new Triple(p.asNode(), VocabPath.joinsWith.asNode(), node);
//            Triple t = new Triple(p.asNode(), VocabPath.isIngoingPredicateOf.asNode(), node);
//            if(!result.contains(t)) {
//                result.add(t);
//
//                Triple u = new Triple(s.asNode(), VocabPath.hasOutgoingPredicate.asNode(), p.asNode());
//                result.add(u);
//                createQueryBackward(model, s.asNode(), p.asResource(), result);
//            }
//        }
//    }

}