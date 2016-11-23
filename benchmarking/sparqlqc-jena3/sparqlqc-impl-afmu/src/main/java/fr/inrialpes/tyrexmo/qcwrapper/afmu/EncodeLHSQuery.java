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

package fr.inrialpes.tyrexmo.qcwrapper.afmu;

import java.util.Stack;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;

import fr.inrialpes.tyrexmo.queryanalysis.TransformAlgebra;

public class EncodeLHSQuery extends QueryToFormula {

	Stack<Object> triples;
	//TODO: transform left query triples into an arraylist
	public EncodeLHSQuery (String leftQuery) {
		TransformAlgebra algebra = new TransformAlgebra(leftQuery);
		triples = algebra.getQueryPattern();
	}

	public EncodeLHSQuery (Query leftQuery) {
		TransformAlgebra algebra = new TransformAlgebra(leftQuery);
		triples = algebra.getQueryPattern();
	}

	public EncodeLHSQuery (Stack<Object> triples) {
		System.out.println(mathcalA(triples));
	}

	public String getFormula () {
		return mathcalA(triples);
	}

	/**
	 *  the (subject, predicate, object) of a triple
	 *  need syntactic formatting in order to match
	 *  the syntactic restrictions of the satisfiability
	 *  solver.
	 */
	protected String formatSubject(Triple t) {
    	String s = "";
    	if (t.getSubject().isVariable())
			//s = t.getSubject().toString();  // remove question mark, in variables
    		s = "var"+t.getSubject().getName().replace("?", "");
    	if (t.getSubject().isURI())
			s = t.getSubject().getLocalName();
		if (t.getSubject().isBlank()){

			s = t.getSubject().getBlankNodeLabel();
//			s = t.getSubject().getName();
			String alphabet = "abcdefghijklmnopqrstuvwxyz";
			int charc = (int)(Math.random()*26);
			String append = alphabet.substring(charc, charc+1);
			s = append + s;
			s = "tebeda";
		}
    	return s;
    }
    protected String formatPredicate(Triple t) {
    	String p ="";
    	if (t.getPredicate().isVariable())
			//p = t.getPredicate().toString();  //remove question mark
    		p = "var"+t.getPredicate().getName();
		if(t.getPredicate().isURI())
			p = t.getPredicate().getLocalName();
    	return p;
    }
    protected String formatObject(Triple t) {
    	String o="";
    	if (t.getObject().isVariable())
			//o =  t.getObject().toString() ;  // remove question mark
//    		o = "var"+t.getObject().getName();
    		o = "var"+t.getObject().getName().replace("?", "");
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
    }
}
