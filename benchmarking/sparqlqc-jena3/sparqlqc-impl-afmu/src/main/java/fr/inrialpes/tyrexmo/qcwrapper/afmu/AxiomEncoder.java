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
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;

public class AxiomEncoder extends QueryToFormula {
	
	private String axiom;
	private Collection<String> axioms = new ArrayList<String> ();
	
	private final String _SUBCLASS = "sc";
	private final String _SUBPROPERTY = "sp";
	private final String _DOM = "dom";
	private final String _RANGE = "range";
	private final String _EQUIV = "equiv"; // denotes equivalence
	
	// Encoding of constraints
	private String phiC;
	
	public AxiomEncoder(String axiom) {
	    System.err.println( "AXIOMENCODER Created" );
		setAxiom(axiom);
		createEncoding() ;
	}
	
	public AxiomEncoder(Collection<String> axioms) {
	    System.err.println( "AXIOMENCODER Created" );
		setAxioms(axioms);
		createEncoding() ;
	}
	
    /**
     * Simulates axiom encoding
     * new AxiomEncoder().encodeSchema( String );
     * Replaces the usual
     * new AxiomEncoder( String ).getFormula();
     * IT IS NOT SUPPOSED TO THROW ContainmentTestException
     * Just to encode schemas
     */
    public AxiomEncoder() {};
    
    public String encodeSchema( String schema ) {
	if ( schema.endsWith( "C1" ) ) return rdfs1();
	else if ( schema.endsWith( "C2" ) ) return rdfs2();
	else if ( schema.endsWith( "C3" ) ) return rdfs3();
	else if ( schema.endsWith( "C4" ) ) return rdfs4();
	else if ( schema.endsWith( "C5" ) ) return rdfs5();
	else return "true";
    }

    private String rdfs1 () {
	// axiom = {(GraduateStudent,sc,Student), (UndergradStudent,sc,Student)}
	String axiom1 =  "(let xnu2 = nu (graduatestudent -> student) & " +
	    "([s]xnu2 & [p]xnu2 &  [o]xnu2 & [-s]xnu2 & [-p]xnu2 &  [-o]xnu2 & [d]xnu2) in xnu2 end)";
	String axiom2 =  "(let xnu3 = nu (undergradstudent -> student) & " +
	    "([s]xnu3 & [p]xnu3 &  [o]xnu3 & [-s]xnu3 & [-p]xnu3 &  [-o]xnu3 & [d]xnu3) in xnu3 end)";
	return "("+axiom1 + " & " + axiom2 + ")";
    }

    private String rdfs2 () {
	// axiom2 = {(headOf,dom,Professor), (headOf,range,Department), (Chair, sc, Professor)}
	//		String axiom1 = "(let xnu5 = nu (<s>(<p>headof -> <p>type & <o>professor)) & " +
	//				"([s]xnu5 & [p]xnu5 &  [o]xnu5 & [-s]xnu5 & [-p]xnu5 &  [-o]xnu5 & [d]xnu5) in xnu5 end)";
	//		String axiom1 = "(let xnu5 = nu (<s><p>headof -> <s><o>professor) & " +
	//		"([s]xnu5 & [p]xnu5 &  [o]xnu5 & [-s]xnu5 & [-p]xnu5 &  [-o]xnu5 & [d]xnu5) in xnu5 end)";
	String axiom1 = "(let xnu5 = nu (<-s>ss &<p>headof & <o>true -> <-s>ss & <p>type & <o>professor) & " +
	    "([s]xnu5 & [p]xnu5 &  [o]xnu5 & [-s]xnu5 & [-p]xnu5 &  [-o]xnu5 & [d]xnu5) in xnu5 end)";
	String axiom2 =  "(let xnu3 = nu (<-s>true & <p>headof & <o>o -><d>(<-s>o & <p>type & <o>department)) & " +
	    "([s]xnu3 & [p]xnu3 &  [o]xnu3 & [-s]xnu3 & [-p]xnu3 &  [-o]xnu3 & [d]xnu3) in xnu3 end)";
	String axiom3 =  "(let xnu2 = nu (chair -> professor) & " +
	    "([s]xnu2 & [p]xnu2 &  [o]xnu2 & [-s]xnu2 & [-p]xnu2 &  [-o]xnu2 & [d]xnu2) in xnu2 end)";
		
	return "("+axiom1 + " & " + axiom2  + " & "+ axiom3 +")";
    }

    private String rdfs3 () {
	// axiom3 = { 
	/*  (maleHeadOf,sp,headOf),
	    (femaleHeadOf,sp,headOf ), 
	    (FullProfessor,sc,Professor),
	    (headOf,dom,FullProfessor)
	    }*/
	String axiom1 =  "(let xnu2 = nu (maleheadof -> headof) & " +
	    "([s]xnu2 & [p]xnu2 &  [o]xnu2 & [-s]xnu2 & [-p]xnu2 &  [-o]xnu2 & [d]xnu2) in xnu2 end)";
	String axiom2 =  "(let xnu3 = nu (femaleheadof -> headof) & " +
	    "([s]xnu3 & [p]xnu3 &  [o]xnu3 & [-s]xnu3 & [-p]xnu3 &  [-o]xnu3 & [d]xnu3) in xnu3 end)";
	String axiom3 =  "(let xnu4 = nu (fullprofessor -> professor) & " +
	    "([s]xnu4 & [p]xnu4 &  [o]xnu4 & [-s]xnu4 & [-p]xnu4 &  [-o]xnu4 & [d]xnu4) in xnu4 end)";
	//		String axiom4 = "(let xnu5 = nu (<s>(<p>headof -> <p>type & <o>fullprofessor)) & " +
	//							"([s]xnu5 & [p]xnu5 &  [o]xnu5 & [-s]xnu5 & [-p]xnu5 &  [-o]xnu5 & [d]xnu5) in xnu5 end)";
	String axiom4 = "(let xnu5 = nu (<-s>true & <p>headof & <o>true -> <-s>true & <p>type & <o>fullprofessor) & " +
	    "([s]xnu5 & [p]xnu5 &  [o]xnu5 & [-s]xnu5 & [-p]xnu5 &  [-o]xnu5 & [d]xnu5) in xnu5 end)";
	
	return "("+axiom1 + " & " + axiom2 + " & " + axiom3 + " & "+ axiom4  + ")";
	//		String axiom = "("+axiom1 + " & " + axiom3 + " & "+ axiom4  + ")";
    }
	
    private String rdfs4 () {
	// axiom4 = {(CsCourse,sc,Course)}
	String axiom1 =  "(let xnu2 = nu (cscourse -> course) & " +
	    "([s]xnu2 & [p]xnu2 &  [o]xnu2 & [-s]xnu2 & [-p]xnu2 &  [-o]xnu2 & [d]xnu2) in xnu2 end)";
	return "("+axiom1 + ")";
    }

    private String rdfs5 () {
	// axiom5 = {trans(sc)}
	String axiom1 =  "(let xnu2 = nu (ss & <s>(<p>sc & <o>(yy & <s>(<p>sc & <o>oo))  -> (ss & <s>(<p>sc & <o>oo)))) & " +
	    "([s]xnu2 & [p]xnu2 &  [o]xnu2 & [-s]xnu2 & [-p]xnu2 &  [-o]xnu2 & [d]xnu2) in xnu2 end)";
	return "("+axiom1 + ")";
    }

	protected void setAxiom(String axiom) {
		this.axiom = axiom;
	}
	protected String getAxiom() {
		return this.axiom;
	}
	
	protected void setAxioms(Collection<String> axioms) {
		this.axioms = axioms;
	}
	protected Collection<String> getAxioms() {
		return this.axioms;
	}
	
	public String getEncoding(String axiomTriple) {
		String formula = "", s, p, o;
		String [] spo = axiomTriple.split(" ");
		if (!(spo.length < 3)) {
			s = spo[0]; p = spo[1]; o = spo[2];
			if ( p.equals(_SUBCLASS) )
				formula = getSubclassFormula(s,o);
			else if ( p.equals(_SUBPROPERTY) )
				formula = getSubpropertyFormula(s,o);
			else if ( p.equals(_DOM) )
				formula = getDomFormula(s,o);
			else if ( p.equals(_RANGE) )	
				formula = getRangeFormula(s,o);
			else if ( p.equals(_EQUIV) )	
				formula = getEquivFormula(s,o);
		}	
		return formula;
	}
	private String getEquivFormula(String s, String o) {
		String var = genVarName();
		muVars.add(var);
		return "(let "+var+" = nu ("+ s + " <-> "+ o +")  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
	}
	private String getSubclassFormula(String s, String o) {
		String var = genVarName();
		muVars.add(var);
		// with all the transition programs
		return "(let "+var+" = nu ("+ s + " -> "+ o +")  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
		/* optimizations */
//		return "(let "+var+" = nu ("+ s + " -> "+ o +")  & (<d>"+ var  +" & <-s>"+ var +" & <p>"+  var +" & <o>"+ var +") in "+ var +" end)";
//		return "(let "+var+" = nu ("+ s + " -> "+ o +")  & ([d]"+ var +" & [-s]"+ var +" & [o]"+ var +") in "+ var +" end)";
		
		// with mu
//		return "!(let "+var+" = mu !("+ s + " -> "+ o +")  | (<d>"+ var  +" | <-s>"+ var +" | <p>"+  var +" | <o>"+ var +") in "+ var +" end)";
		
		// variations
//		return "!(let "+var+" = mu ("+ s + " & ! "+ o +")  | (<d>"+ var  + " | <s>"+ var +" | <-s>"+ var +" | <p>"+  var + "| <-p>"+ var +" | <o>"+ var + "| <-o>"+ var +") in "+ var +" end)";
	}
	
	private String getSubpropertyFormula(String p, String q) {
		String var = genVarName();
		muVars.add(var);
		// with all the transition programs
//		return "(let "+var+" = nu ("+ p + " -> "+ q +")  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
		/*  optimizations  */
		return "(let "+var+" = nu ("+ p + " -> "+ q +")  & ([d]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [o]"+ var +") in "+ var +" end)";
	}
	
	private String getDomFormula(String r, String c) {
		String var = genVarName();
		muVars.add(var);
		// with all the transition programs
		return "(let "+var+" = nu (<s>(<p>"+ r + " -> <p>type & <o>"+ c +"))  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";		
//		return "(let "+var+" = nu (<s><p>"+ r + " -> <s>(<p>type & <o>"+ c +"))  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
//		return "(let "+var+" = nu (aps & <s>(<p>"+ r + " & <o>apo) -> aps & <s>(<p>type & <o>"+ c +"))  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
//		return "(let "+var+" = nu (true & <s>(<p>"+ r + " & <o>true) -> true & <s>(<p>type & <o>"+ c +"))  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
//		return "(let "+var+" = nu (<-s>true & <p>"+ r + " & <o>true) -> <d>(<-s>true & <p>type & <o>"+ c +")  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
		// doesnot work
//		return "(let "+var+" = nu (<s><p>"+ r + " -> <s>(<p>type & <o>"+ c +"))  & ([d]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [o]"+ var +") in "+ var +" end)";
		/* optimization */
//		return "(let "+var+" = nu (<s><p>"+ r + " ->  <s><o>"+ c +")  & ([d]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [o]"+ var +") in "+ var +" end)"; //doesn't work
//		return "(let "+var+" = nu (<s><p>"+ r + " ->  <s><o>"+ c +")  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
		
	}
	
	private String getRangeFormula(String r, String c) {
		String var = genVarName();
		muVars.add(var);
		// with all the transition programs
		return "(let "+var+" = nu (<-o><p>"+ r + " -> <s>(<p>type & <o>"+ c +"))  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
		
		//optimization
//		return "(let "+var+" = nu (<-o><p>"+ r + " -> <s><o>"+ c +")  & ([d]"+ var +" & [s]"+ var +" & [-s]"+ var +" & [p]"+ var +" & [-p]"+ var+" & [o]"+ var +" & [-o]"+ var +") in "+ var +" end)";
	}
	
	protected String conjunctEncodingOfAxioms (List<String> axiomEncodings ) {
		String formula = "";
		
		if (!axiomEncodings.isEmpty()) {
			formula += axiomEncodings.get(0);
			
			for (int i = 1; i < axiomEncodings.size(); i++)
				formula += " & " + axiomEncodings.get(i); 			
		}
		
		return formula;
	}
	
	private void createEncoding ( ) {
		List<String> formulae = new ArrayList<String> ();
		if (this.axiom == null && this.axioms == null )
			return;
		else if (this.axiom != null )
			formulae.add( getEncoding(getAxiom()) );
		else if ( this.axioms != null ) {
			for ( String c : this.axioms ) {
				formulae.add( getEncoding(c) );
			}
		}
		
		setFormula( conjunctEncodingOfAxioms(formulae) );
	}
	
	private void setFormula( String f ) {
		this.phiC = f;
	}
	
	public String getFormula() {
		return this.phiC;
	}
	
	
	@Override
	protected String formatSubject(Triple t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String formatPredicate(Triple t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String formatObject(Triple t) {
		// TODO Auto-generated method stub
		return null;
	}
}
