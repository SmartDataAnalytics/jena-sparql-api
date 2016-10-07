/*
 * Copyright (C) INRIA, 2012-2013
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.tyrexmo.qcwrapper.lmu;


import java.util.Collection;
import java.util.List;
import java.util.Stack;


import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;

import fr.inrialpes.tyrexmo.queryanalysis.TransformAlgebra;


public class EncodeRHSQuery extends QueryToFormula {
	private TransformAlgebra algebra;
	private Collection<Var> ndvar;
	private Stack<Object> triples;
	private List<String> distVars;
	
	public EncodeRHSQuery (String q) {
		algebra = new TransformAlgebra(q);
		ndvar = algebra.getNonDistVars();
		triples = algebra.getQueryPattern();
		distVars = algebra.getProjectVars();		
	}
	
	public EncodeRHSQuery (Query q) {
		algebra = new TransformAlgebra(q);
		ndvar = algebra.getNonDistVars();
		triples = algebra.getQueryPattern();
		distVars = algebra.getProjectVars();
	}
	
	public EncodeRHSQuery ( TransformAlgebra alg ) {
		algebra = alg;
		ndvar = algebra.getNonDistVars();
		triples = algebra.getQueryPattern();
		distVars = algebra.getProjectVars();
	}
	
	protected String getFormula () {
		return mathcalA(triples);
	}
	
	/**
	 *  the (subject, predicate, object) of a triple 
	 *  need syntactic formatting in order to match
	 *  the syntactic restrictions of the satisfiability 
	 *  solver.
	 */
	/*protected String formatSubject(Triple t) {
    	String s = "";
    	if (t.getSubject().isVariable()) {
			//s = t.getSubject().toString();  // remove question mark, in variables    		
    		Var v = (Var) t.getSubject();
    		if (ndvar.contains(v))
    			s = "true";           // if v is ndvar, v is encoded as \top
    		else 
    			s = "var"+t.getSubject().getName();	
    	}
    	if (t.getSubject().isURI())
			s = t.getSubject().getLocalName();
		if (t.getSubject().isBlank()){
			s = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			s = append + s;
		}
    	return s;
    }
	protected String formatPredicate(Triple t) {
    	String p ="";
    	if (t.getPredicate().isVariable()) {
			//p = t.getPredicate().toString();  //remove question mark
    		Var v = (Var) t.getPredicate();
    		if (ndvar.contains(v))
    			p = "true";           // if v is ndvar, v is encoded as \top
    		else 
    			p = "var"+t.getPredicate().getName();
    	}
		if(t.getPredicate().isURI()) 
			p = t.getPredicate().getLocalName();
    	return p;
    }
	protected String formatObject(Triple t) {
    	String o="";
    	if (t.getObject().isVariable()) {
			//o =  t.getObject().toString() ;  // remove question mark
    		Var v = (Var) t.getObject();
    		if (ndvar.contains(v))
    			o = "true";           // if v is ndvar, v is encoded as \top
    		else  {
//    			o =  "var"+t.getObject().toString().replaceAll("?", "");
    			o = "var"+t.getObject().getName().replace("?", "");
    		}
    	}
		if (t.getObject().isURI()) 
			o = t.getObject().getLocalName();
		if (t.getObject().isLiteral())
			o = t.getObject().getLiteralLexicalForm();
		if (t.getObject().isBlank()){
			o = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			o = append + o;
		}
    	return o;
    } */
	
	/*
	 *  for testing containment b/n queries containing different distinguished variables
	 */
	/*protected String formatSubject(Triple t) {
    	String s = "";
    	if (t.getSubject().isVariable()) {
			s = "true";           // if v is ndvar, v is encoded as \top
    			
    	}
    	if (t.getSubject().isURI())
			s = t.getSubject().getLocalName();
		if (t.getSubject().isBlank()){
			s = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			s = append + s;
		}
    	return s;
    }
	protected String formatPredicate(Triple t) {
    	String p ="";
    	if (t.getPredicate().isVariable()) {
			p = "true";           // if v is ndvar, v is encoded as \top
    		
    	}
		if(t.getPredicate().isURI()) 
			p = t.getPredicate().getLocalName();
    	return p;
    }
	protected String formatObject(Triple t) {
    	String o="";
    	if (t.getObject().isVariable()) {
			o = "true";           // if v is ndvar, v is encoded as \top
    		 	}
		if (t.getObject().isURI()) 
			o = t.getObject().getLocalName();
		if (t.getObject().isLiteral())
			o = t.getObject().getLiteralLexicalForm();
		if (t.getObject().isBlank()){
			o = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			o = append + o;
		}
    	return o;
    }*/
	
	/*
	 *  encode(dvar) = dvar, and encode(ndvar) = true;
	 */
	protected String formatSubject(Triple t) {
    	String s = "";
    	if (t.getSubject().isVariable()) {
    		if (distVars.contains(t.getSubject().getName()))
    			s = "_var"+t.getSubject().getName();	
    		else 
    			s = "T";
    			
    	}
    	if (t.getSubject().isURI())
			s = "_"+t.getSubject().getLocalName();
		if (t.getSubject().isBlank()){
			s = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			s = "_"+append + s;
		}
    	return s;
    }
	protected String formatPredicate(Triple t) {
    	String p ="";
    	if (t.getPredicate().isVariable()) {
    		if (distVars.contains(t.getPredicate().getName()))
    			p = "_var"+t.getPredicate().getName();	
    		else 
    			p = "T";
    		
    	}
		if(t.getPredicate().isURI()) 
			p = "_"+t.getPredicate().getLocalName();
    	return p;
    }
	protected String formatObject(Triple t) {
    	String o="";
    	if (t.getObject().isVariable()) {
    		if (distVars.contains(t.getObject().getName()))
    			o = "_var"+t.getObject().getName();	
    		else 
    			o = "T";
    	}
		if (t.getObject().isURI()) 
			o = "_"+t.getObject().getLocalName();
		if (t.getObject().isLiteral())
			o = "_"+t.getObject().getLiteralLexicalForm();
		if (t.getObject().isBlank()){
			o = t.getSubject().getBlankNodeLabel();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			o = "_"+append + o;
		}
    	return o;
    }

}
