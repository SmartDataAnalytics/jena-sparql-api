package org.aksw.jena_sparql_api.views;

import java.util.Set;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.expr.ExprList;



public class TestElementTransformRewriteQueryOverViews {

	
	//@Test
	public void testRewriteQueryOverViews() {
		CandidateViewSelectorSparqlView cs = new CandidateViewSelectorSparqlView();
		QuadPattern qp = new QuadPattern();
		qp.add(new Quad(Vars.g, Vars.s, Vars.p, Vars.o));
	
		//ExprList constraints = new ExprList();
		//constraints.add(new E_Equals(new ExprVar(Vars.p), NodeValue.makeNode(RDF.type.asNode())));
		
		
		
		
		//NodeValue.TRUE
		cs.addView(new SparqlView("test", qp, new ExprList(), new VarDefinition(), OpNull.create()));
		
		Set<ViewQuad<SparqlView>> cands = cs.findCandidates(new Quad(Vars.l, Vars.x, Vars.y, Vars.z), new RestrictionManagerImpl());
		
		for(ViewQuad<SparqlView> cand : cands) {
			System.out.println("Cand: " + cand);
		}
		
		
		
	}
}
