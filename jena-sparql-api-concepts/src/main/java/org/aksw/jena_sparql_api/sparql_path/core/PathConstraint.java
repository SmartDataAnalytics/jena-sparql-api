package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.sparql_path.core.domain.Concept;
import org.aksw.jena_sparql_api.sparql_path.utils.ElementTreeAnalyser;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.ReplaceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

public class PathConstraint {
	
	private static final Logger logger = LoggerFactory.getLogger(PathConstraint.class);
	
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
	
	public static final String varNs = "http://dummy.org/var/";
	
	public static Var uriToVar(Node node) {
		Var result = null;
		
		if(node.isVariable()) {
			result = (Var)node;
		}
		else if(node.isURI()) {
			String tmp = node.getURI();
			if(tmp.startsWith(varNs)) {
				String suffix = tmp.substring(varNs.length());
				
				result = Var.alloc(suffix);
			}
		}
		
		return result;
	}
	
	public static Node createVarUri(Var var) {
		return Node.createURI(varNs + var.getName());
	}
	
	public static Quad createUriVars(Quad quad) {

	    Node g = quad.getGraph();
	    Node s = quad.getSubject();
        Node p = quad.getPredicate();
	    Node o = quad.getObject();
	    
        if(g.isVariable()) { g = createVarUri((Var)g); }
        if(s.isVariable()) { s = createVarUri((Var)s); }
        // Leave predicate untouched
	    if(o.isVariable()) { o = createVarUri((Var)o); }
	    
		
		Quad result = new Quad(g, s, p, o);
		return result;
	}

	public static Concept getPathConstraintsSimple(Concept concept) {
		Model model = ModelFactory.createDefaultModel();

		
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
		
		for(Quad quad : quads) {
			// Replace variables with fake uris
			Quad q = createUriVars(quad);
			
						//if(!q.getPredicate().isVariable()) {
			
    			Statement stmt = model.asStatement(q.asTriple());
    			model.add(stmt);
			//}
		}
		
	
		//Node s = createVarUri(concept.getVar());
		//Resource start = model.asRDFNode(s).asResource();
		
		Set<Triple> result = new HashSet<Triple>();
		createQueryForward(model, concept.getVar(), VocabPath.start, result);
		createQueryBackward(model, concept.getVar(), VocabPath.start, result);
		
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


	public static void createQueryForward(Model model, Node node, Resource res, Set<Triple> result) {
		Node n;
		if(node.isVariable()) {
			n = createVarUri((Var)node);
		} else {
			n = node;
		}
		
		Resource r = model.asRDFNode(n).asResource();

		
		Set<Statement> succs = model.listStatements(r, null, (RDFNode)null).toSet();

		
		for(Statement stmt : succs) {
			RDFNode oo = stmt.getObject();
			if(!oo.isURIResource()) {
				continue;
			}

			Property p = stmt.getPredicate();
			Resource o = oo.asResource();
		
			Triple t = new Triple(node, VocabPath.joinsWith.asNode(), p.asNode());
			if(!result.contains(t)) {			
				result.add(t);
				createQueryForward(model, o.asNode(), p.asResource(), result);
			}
		}
	}		
	
	public static void createQueryBackward(Model model, Node node, Resource res, Set<Triple> result) {
		Node n;
		if(node.isVariable()) {
			n = createVarUri((Var)node);
		} else {
			n = node;
		}
		
		Resource r = model.asRDFNode(n).asResource();

		Set<Statement> preds = model.listStatements(null, null, r).toSet();
		for(Statement stmt : preds) {
			Resource s = stmt.getSubject();
			Property p = stmt.getPredicate();
		
			Triple t = new Triple(p.asNode(), VocabPath.joinsWith.asNode(), node);
			if(!result.contains(t)) {
				result.add(t);
				createQueryBackward(model, s.asNode(), p.asResource(), result);
			}
		}
	}

}