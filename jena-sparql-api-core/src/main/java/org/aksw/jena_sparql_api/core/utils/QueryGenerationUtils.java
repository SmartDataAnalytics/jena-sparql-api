package org.aksw.jena_sparql_api.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.utils.GeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;

public class QueryGenerationUtils {

    // Util for cerateQueryCount
    public static Query wrapAsSubQuery(Query query, Var v) {
        Element esq = new ElementSubQuery(query);

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(v);
        result.setQueryPattern(esq);

        return result;
    }


    /**
     * Takes a concept and adds
     *
     * @return
     */
    public static Concept createPropertyQuery(Concept concept) {
        Collection<Var> vars = PatternVars.vars(concept.getElement());
        List<String> varNames = VarUtils.getVarNames(vars);

        Var s = concept.getVar();

        Generator gen = GeneratorBlacklist.create("v", varNames);
        Var p = Var.alloc(gen.next());
        Var o = Var.alloc(gen.next());


        Triple triple = new Triple(s, p, o);

        BasicPattern bp = new BasicPattern();
        bp.add(triple);

        List<Element> elements;
        if(concept.isSubjectConcept()) {
            elements = new ArrayList<Element>();
        } else {
            elements = concept.getElements();
        }
        elements.add(new ElementTriplesBlock(bp));

        Concept result = new Concept(elements, p);

        return result;
    }
}
