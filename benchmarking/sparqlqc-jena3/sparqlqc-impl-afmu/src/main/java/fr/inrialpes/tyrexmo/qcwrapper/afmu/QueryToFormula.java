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

import java.util.ArrayList;
import java.util.Stack;

import com.hp.hpl.jena.graph.Triple;

public abstract class QueryToFormula {
    // Rewriten JE
    //protected static HashSet<String> muVars = new HashSet<String>();
    protected static int varRank = 0;

    protected String genVarName() { return "v"+varRank++; }

	// generate unique recursive variables
    // JE: using static here is criminal!
    //protected static ArrayList<String> muVars = new ArrayList<String> ();
	protected ArrayList<String> muVars = new ArrayList<String> ();
	
	/**
	 *  Adds a mu recursion to a formula, according to the syntax of 
	 *  the satisfiability solver from [tanabe-et-al:2005]
	 *  
	 * @param formula : a formula as a string of characters
	 * @return returns a recursive formula in a string format. 
	 */
	protected String addMu (String formula) {
		String var = genVarName();
		muVars.add(var);
		
		// with all the transition programs 
//		return "(let "+var+" = mu ("+ formula +")  | (<d>"+ var+ "| <-d>"+ var +" | <s>"+ var + " | <-s>"+ var +" | <p>"+ var+" | <-p>" + var +" | <o>"+ var+" | <-o>"+ var +") in "+ var +" end)";
		// with all the transition programs 
//		return "(let "+var+" = mu ("+ formula +")  | (<d>"+ var +" | <s>"+ var + " | <-s>"+ var +" | <p>"+ var+" | <-p>" + var +" | <o>"+ var+" | <-o>"+ var +") in "+ var +" end)";
		//without 'd'
//		return "(let "+var+" = mu ("+ formula +")  | " +"(<s>"+ var + " | <-s>"+ var +" | <p>"+ var+" | <-p>" + var +" | <o>"+ var+" | <-o>"+ var +") in "+ var +" end)";
		
		//optimized version
//		return "(let "+var+" = mu ("+ formula +")  | (<d>"+ var +" | <s>"+ var +" | <-p>"+ var +" | <-o>"+ var +") in "+ var +" end)";
		
		//optimized version
//		return "(let "+var+" = mu ("+ formula +")  | (<d>"+ var +"| <-d>"+ var +"| <s>"+ var +" | <-p>"+ var +" | <-o>"+ var +") in "+ var +" end)";
		
		// with only program 'd'
		return "(let "+var+" = mu ("+ formula +")  | <d>"+ var +" in "+ var +" end)" ;
		
		// only forward modalities
//		return "(let "+var+" = mu ("+ formula +")  | (<d>"+ var +" | <-d>"+ var +") in "+ var +" end)";
		
		// without program 'd'
//		return "(let "+var+" = mu ("+ formula +")  | ("+ " <s>"+ var +" | <-p>"+ var +" | <-o>"+ var +") in "+ var +" end)";
		// pierre's approach
//		return "(let $"+var+" =  ("+ formula +")  | <1>$"+ var+ " | <2>$"+ var +" in $"+ var +")" ;
	}
	/**
	 * Generates a random unique variable name for mu formula.
	 * @return a string variable name
	 *//*
	protected String genVarName() {
		String vars = "xyz";
		String nums = "0123456789";
		String varName = null;
		boolean unique = true;
		while (unique) {
			int character = (int)(Math.random()*3);				
			int num = (int)(Math.random()*10);
			varName = vars.substring(character,character+1) + nums.substring(num, num+1);
			if(!muVars.contains(varName))
				unique = false;
		}
		return varName;
		}*/
	/**
	 * this function takes a triple and creates a conjunctive formula
	 * as a string
	 * @param t : A SPARQL triple
	 * @return a mu-calculus formula as a string.
	 */
	protected String createFormula(Triple t) {
		String s, p, o;
		s = formatSubject(t);
		p = formatPredicate(t);
		o = formatObject(t);
		
		return "(<-s>" + s.toLowerCase() +" & " + "<p>"+ p.toLowerCase() +" & " + "<o>" + o.toLowerCase()+")";
		//  pierre's approach
//		return  s.toLowerCase() +" & <1>(<1>"+ p.toLowerCase() +" & <2>" + o.toLowerCase()+")";
	}
	
	protected abstract String formatSubject(Triple t);
	protected abstract String formatPredicate(Triple t);
	protected abstract String formatObject(Triple t);
	
	/**
	 *  takes an algebra of a query and generates a mu-calculus formula.
	 *  
	 * @param pattern : a SPARQL query pattern
	 * @return : a formula as a string of characters.
	 */
	
	protected String mathcalA(Stack<Object> pattern) {
		Stack<String> formula = new Stack<String>();
		while (!pattern.isEmpty()) {
			if (pattern.peek().equals("UNION")) {
				formula.push((String)pattern.pop());
				//formula.push("("+addMu(createFormula((Triple)pattern.pop())) + " | " +
					//	addMu(createFormula((Triple)pattern.pop())) + ")");
			} else if (pattern.peek().equals("AND")) {
					formula.push((String)pattern.pop());
			}
			else if (pattern.peek().equals("MINUS")) {
				formula.push((String)pattern.pop());
			}
			else if (pattern.peek() instanceof Triple) { 
				formula.push(addMu(createFormula((Triple)pattern.pop())));
			} else {
				formula.push((String)pattern.pop());
			}
		}
		//System.out.println(formula);
		String f ="";
//		if (!formula.isEmpty()) {
//			if (formula.size() == 1)
//				f = formula.pop();
//			else {
//				f = formula.pop();
//				while (!formula.isEmpty())
//					f += " & " + formula.pop();
//			}
//		}
		if (!formula.isEmpty()) {
			if (formula.size() == 1)
				f = formula.pop();
			else {
				while (!formula.isEmpty()) {
					if (formula.peek().equals("UNION")) {
						f += " | ";
						formula.pop();
					}
					else if (formula.peek().equals("AND")) {
						f += " & ";
						formula.pop();
					}
					else if (formula.peek().equals("MINUS")) {
						f += " & !";
						formula.pop();
					}
					else 
						f += formula.pop();
				}
			}
		}
		return f;
	} 
	
	/*protected String mathcalA(Stack<Object> pattern) {
		Stack<String> formula = new Stack<String>();
		while (!pattern.isEmpty()) {
			if (pattern.peek().equals("UNION")) {
				formula.push((String)pattern.pop());
				//formula.push("("+addMu(createFormula((Triple)pattern.pop())) + " | " +
					//	addMu(createFormula((Triple)pattern.pop())) + ")");
			} else if (pattern.peek().equals("AND")) {
					formula.push((String)pattern.pop());
			}
			else if (pattern.peek().equals("MINUS")) {
				formula.push((String)pattern.pop());
			}
			else if (pattern.peek() instanceof Triple) { 
				//formula.push(addMu(createFormula((Triple)pattern.pop())));
				formula.push("<d>"+createFormula((Triple)pattern.pop()));
			} else {
				formula.push((String)pattern.pop());
			}
		}
		String f ="";
		if (!formula.isEmpty()) {
			if (formula.size() == 1)
				f = formula.pop();
			else {
				while (!formula.isEmpty()) {
					if (formula.peek().equals("UNION")) {
						f += " | ";
						formula.pop();
					}
					else if (formula.peek().equals("AND")) {
						f += " & ";
						formula.pop();
					}
					else if (formula.peek().equals("MINUS")) {
						f += " & !";
						formula.pop();
					}
					else 
						f += formula.pop();
				}
			}
		}
		return addMu(f);
	}*/
	//focus s'' node
	/*protected String mathcalA(Stack<Object> pattern) {
		Stack<String> formula = new Stack<String>();
		while (!pattern.isEmpty()) {
			if (pattern.peek().equals("UNION")) {
				formula.push((String)pattern.pop());
				//formula.push("("+addMu(createFormula((Triple)pattern.pop())) + " | " +
					//	addMu(createFormula((Triple)pattern.pop())) + ")");
			} else if (pattern.peek().equals("AND")) {
					formula.push((String)pattern.pop());
			}
			else if (pattern.peek().equals("MINUS")) {
				formula.push((String)pattern.pop());
			}
			else if (pattern.peek() instanceof Triple) { 
				//formula.push(addMu(createFormula((Triple)pattern.pop())));
				formula.push("<d>"+createFormula((Triple)pattern.pop()));
			} else {
				formula.push((String)pattern.pop());
			}
		}
		String f ="";
		if (!formula.isEmpty()) {
			if (formula.size() == 1)
				f = formula.pop();
			else {
				while (!formula.isEmpty()) {
					if (formula.peek().equals("UNION")) {
						f += " | ";
						formula.pop();
					}
					else if (formula.peek().equals("AND")) {
						f += " & ";
						formula.pop();
					}
					else if (formula.peek().equals("MINUS")) {
						f += " & !";
						formula.pop();
					}
					else 
						f += formula.pop();
				}
			}
		}
		return addMu(f);
//		return f;
	}*/
	protected abstract String getFormula();
	
	
}
