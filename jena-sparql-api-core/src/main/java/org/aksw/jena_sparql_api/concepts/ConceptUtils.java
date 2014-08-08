package org.aksw.jena_sparql_api.concepts;


public class ConceptUtils {
    public static Concept listDeclaredProperties = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || ?t = <http://www.w3.org/2002/07/owl#ObjectProperty> || ?t = <http://www.w3.org/2002/07/owl#DataTypeProperty>)", "s");
    public static Concept listDeclaredClasses = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/2000/01/rdf-schema#Class> || ?t = <http://www.w3.org/2002/07/owl#Class>)", "s");
    public static Concept listUsedClasses = Concept.create("?s a ?t", "t");
    
    public static Concept listAllPredicates = Concept.create("?s ?p ?o", "p");
    public static Concept listAllGraphs = Concept.create("Graph ?g { ?s ?p ?o }", "g");
    
    /**
     * Select Distinct ?g { Graph ?g { ?s ?p ?o } }
     * 
     * @return
     */
    /*
    public static Concept listGraphs() {

        Triple triple = new Triple(Vars.s, Vars.p, Vars.o);
        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);

        
        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);

        ElementNamedGraph eng = new ElementNamedGraph(Vars.g, group);

        Concept result = new Concept(eng, Vars.g);
        return result;
    }
    */

}
