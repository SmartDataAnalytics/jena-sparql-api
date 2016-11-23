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
		createEncoding () ;
	}
	
	public AxiomEncoder(Collection<String> axioms) {
	    System.err.println( "AXIOMENCODER Created" );
		setAxioms(axioms);
		createEncoding () ;
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
	else return "true";
    }

    private String rdfs1 () {
	// axiom = {(GraduateStudent,sc,Student), (UndergradStudent,sc,Student)}
	String axiom1 =  "~(let $X = (_GraduateStudent & ~_Student) | <1>$X | <-1>$X | <2>$X | <-2>$X in $X)";
	String axiom2 =  "~(let $Y = (_UndergradStudent & ~_Student) | <1>$Y | <-1>$Y | <2>$Y | <-2>$Y in $Y)";
	return "("+axiom1 + " & " + axiom2 + ")";
    }
    
    private String rdfs2 () {
	// axiom2 = {(headOf,dom,Professor), (headOf,range,Department), (Chair, sc, Professor)}
	String axiom1 =  "~(let $Y = (<1>_headOf & ~<1>(_type & <2>_Professor)) | <1>$Y | <-1>$Y | <2>$Y | <-2>$Y in $Y)";
	String axiom2 =  "~(let $Y = (_headOf & ~<2><1>(_type & <2>_Department)) | <1>$Y | <-1>$Y | <2>$Y | <-2>$Y in $Y)";
	String axiom3 = "~(let $Z = (_Chair & ~_Professor) | <1>$Z | <-1>$Z | <2>$Z | <-2>$Z in $Z)";
	return "("+axiom1 + " & " + axiom2  + " & " + axiom3 + ")";
    }

    private String rdfs3 () {
	// axiom3 = { 
	/*  (maleHeadOf,sp,headOf),
	    (femaleHeadOf,sp,headOf ), 
	    (FullProfessor,sc,Professor),
	    (headOf,dom,FullProfessor)
	    }*/
		
	String axiom1 =  "~(let $X = (_maleHeadOf & ~_headOf) | <1>$X | <-1>$X | <2>$X | <-2>$X in $X)";
	String axiom2 =  "~(let $Y = (_femaleHeadOf & ~_headOf) | <1>$Y | <-1>$Y | <2>$Y | <-2>$Y in $Y)";		
	String axiom4 =  "~(let $Z = (<1>_headOf & ~<1>(_type & <2>_FullProfessor)) | <1>$Z | <-1>$Z | <2>$Z | <-2>$Z in $Z)";
	String axiom3 =  "~(let $R = (_FullProfessor & ~_Professor) | <1>$R | <-1>$R | <2>$R | <-2>$R in $R)";
	//String axiom4 =  "~(let $Y = (<1>_headOf & ~<1>(_type & <2>_FullProfessor)) | <1>$Y | <-1>$Y | <2>$Y | <-2>$Y in $Y)";
		
	return "("+axiom1 + " & " + axiom2 + " & " + axiom3 + " & "+ axiom4  + ")";
	// String axiom = "("+axiom1 +  " & " + axiom3 + " & "+ axiom4  + ")";
    }	
	
    private String rdfs4 () {
	// axiom4 = {(CsCourse,sc,Course)}
	String axiom1 =  "~(let $X = (_CsCourse & ~_Course) | <1>$X | <-1>$X | <2>$X | <-2>$X in $X)";
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
