package org.aksw.jena_sparql_api.concepts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.GeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Triples;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

public class ConceptUtils {
    public static Concept listDeclaredProperties = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || ?t = <http://www.w3.org/2002/07/owl#ObjectProperty> || ?t = <http://www.w3.org/2002/07/owl#DataTypeProperty>)", "s");
    public static Concept listDeclaredClasses = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/2000/01/rdf-schema#Class> || ?t = <http://www.w3.org/2002/07/owl#Class>)", "s");
    public static Concept listUsedClasses = Concept.create("?s a ?t", "t");

    public static Concept listAllPredicates = Concept.create("?s ?p ?o", "p");
    public static Concept listAllGraphs = Concept.create("Graph ?g { ?s ?p ?o }", "g");

    public static Set<Var> getVarsMentioned(Concept concept) {
        Collection<Var> tmp = PatternVars.vars(concept.getElement());
        Set<Var> result = SetUtils.asSet(tmp);
        return result;
    }

    public static Concept createSubjectConcept() {
        ElementTriplesBlock e = new ElementTriplesBlock();
        e.addTriple(Triples.spo);
        Concept result = new Concept(e, Vars.s);
        return result;
    }

    public static Map<Var, Var> createDistinctVarMap(Set<Var> workload, Set<Var> blacklist, Generator generator) {
        Set<String> varNames = new HashSet<String>(VarUtils.getVarNames(blacklist));
        Generator gen = new GeneratorBlacklist(generator, varNames);

        Map<Var, Var> result = new HashMap<Var, Var>();
        for(Var var : workload) {
            boolean isBlacklisted = blacklist.contains(var);

            Var t;
            if(isBlacklisted) {
                String name = generator.next();
                t = Var.alloc(name);
            } else {
                t = var;
            }

            result.put(var, t);
        }

        return result;
    }

    /**
     * Creates a generator that does not yield variables part of the concept (at the time of creation)
     * @param concept
     * @return
     */
    public static Generator createGenerator(Concept concept) {
        Collection<Var> tmp = PatternVars.vars(concept.getElement());
        List<String> varNames = VarUtils.getVarNames(tmp);

        Generator base = Gensym.create("v");
        Generator result = new GeneratorBlacklist(base, varNames);

        return result;
    }

    // Create a fresh var that is not part of the concept
    public static Var freshVar(Concept concept) {
        Generator gen = createGenerator(concept);
        String varName = gen.next();
        Var result = Var.alloc(varName);
        return result;
    }

    public static Concept renameVar(Concept concept, Var targetVar) {

        Concept result;
        if(concept.getVar().equals(targetVar)) {
            // Nothing to do since we are renaming the variable to itself
            result = concept;
        } else {
            // We need to rename the concept's var, thereby we need to rename
            // any occurrences of targetVar
            Set<Var> conceptVars = getVarsMentioned(concept);
            Map<Var, Var> varMap = createDistinctVarMap(conceptVars, Collections.singleton(targetVar), Gensym.create("v"));
            varMap.put(concept.getVar(), targetVar);
            Element replElement = ElementUtils.substituteNodes(concept.getElement(), varMap);
            Var replVar = varMap.get(concept.getVar());
            result = new Concept(replElement, replVar);
        }

        return result;
    }

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



    public static Map<Var, Var> createVarMap(Concept attrConcept, Concept filterConcept) {
        Element attrElement = attrConcept.getElement();
        Element filterElement = filterConcept.getElement();

        Collection<Var> attrVars = PatternVars.vars(attrElement);
        Collection<Var> filterVars = PatternVars.vars(filterElement);

        List<Var> attrJoinVars = Collections.singletonList(attrConcept.getVar());
        List<Var> filterJoinVars = Collections.singletonList(filterConcept.getVar());


        Map<Var, Var> result = VarUtils.createJoinVarMap(attrVars, filterVars, attrJoinVars, filterJoinVars, null); //, varNameGenerator);

        return result;
    }

    public static Concept createRenamedConcept(Concept attrConcept, Concept filterConcept) {

        Map<Var, Var> varMap = createVarMap(attrConcept, filterConcept);

        Var attrVar = attrConcept.getVar();
        Element filterElement = filterConcept.getElement();
        Element newFilterElement = ElementUtils.createRenamedElement(filterElement, varMap);

        Concept result = new Concept(newFilterElement, attrVar);

        return result;
    }

    public static Concept createCombinedConcept(Concept attrConcept, Concept filterConcept, boolean renameVars, boolean attrsOptional, boolean filterAsSubquery) {
        // TODO Is it ok to rename vars here? // TODO The variables of baseConcept and tmpConcept must match!!!
        // Right now we just assume that.
        Var attrVar = attrConcept.getVar();
        Var filterVar = filterConcept.getVar();

        if(!filterVar.equals(attrVar)) {
            Map<Var, Var> varMap = new HashMap<Var, Var>();
            varMap.put(filterVar, attrVar);

            // If the attrVar appears in the filterConcept, rename it to a new variable
            Var distinctAttrVar = Var.alloc("cc_" + attrVar.getName());
            varMap.put(attrVar, distinctAttrVar);

            // TODO Ensure uniqueness
            //filterConcept.getVarsMentioned();
            //attrConcept.getVarsMentioned();
            // VarUtils.freshVar('cv', );  //

            filterConcept = this.renameVars(filterConcept, varMap);
        }

        Concept tmpConcept;
        if(renameVars != null) {
            tmpConcept = createRenamedConcept(attrConcept, filterConcept);
        } else {
            tmpConcept = filterConcept;
        }


        List<Element> tmpElements = tmpConcept.getElements();


        // Small workaround (hack) with constraints on empty paths:
        // In this case, the tmpConcept only provides filters but
        // no triples, so we have to include the base concept
        //var hasTriplesTmp = tmpConcept.hasTriples();
        //hasTriplesTmp &&
        Element attrElement = attrConcept.getElement();

        Element e;
        if(tmpElements.length > 0) {

            if(tmpConcept.isSubjectConcept()) {
                e = attrConcept.getElement(); //tmpConcept.getElement();
            } else {

                List<Element> newElements = new ArrayList<Element>();

                if(attrsOptional) {
                    attrElement = new ElementOptional(attrConcept.getElement());
                }
                newElements.add(attrElement);

                if(filterAsSubquery) {
                    tmpElements = [new ElementSubQuery(tmpConcept.asQuery())];
                }


                //newElements.push.apply(newElements, attrElement);
                newElements.push.apply(newElements, tmpElements);


                e = new ElementGroup(newElements);
                e = e.flatten();
            }
        } else {
            e = attrElement;
        }

        Concept result = new Concept(e, attrVar);

        return result;
    }


    public static boolean isGroupedOnlyByVar(Query query, Var groupVar) {
        boolean result = false;

        boolean hasOneGroup = query.getGroupBy().size() == 1;
        if(hasOneGroup) {
            Expr expr = query.getGroupBy().getExprs().values().iterator().next();
            if(expr instanceof ExprVar) {
                Var v = expr.asVar();

                result = v.equals(groupVar);
            }
        }

        return result;
    }

    public static boolean isDistinctConceptVar(Query query, Var conceptVar) {
        boolean isDistinct = query.isDistinct();

        Collection<Var> projectVars = query.getProjectVars();

        boolean hasSingleVar = !query.isQueryResultStar() && projectVars != null && projectVars.size() == 1;
        boolean result = isDistinct && hasSingleVar && projectVars.iterator().next().equals(conceptVar);
        return result;
    }

    public static boolean isConceptQuery(Query query, Var conceptVar) {
        boolean isDistinctGroupByVar = isGroupedOnlyByVar(query, conceptVar);
        boolean isDistinctConceptVar = isDistinctConceptVar(query, conceptVar);

        boolean result = isDistinctGroupByVar || isDistinctConceptVar;
        return result;
    }

    public static Query createAttrQuery(Query attrQuery, Var attrVar, boolean isLeftJoin, Concept filterConcept, Long limit, Long offset, boolean forceSubQuery) {

        Concept attrConcept = new Concept(new ElementSubQuery(attrQuery), attrVar);

        Concept renamedFilterConcept = ConceptUtils.createRenamedConcept(attrConcept, filterConcept);
        //console.log('attrConcept: ' + attrConcept);
        //console.log('filterConcept: ' + filterConcept);
        //console.log('renamedFilterConcept: ' + renamedFilterConcept);

        // Selet Distinct ?ori ?gin? alProj { Select (foo as ?ori ...) { originialElement} }

        // Whether each value for attrVar uniquely identifies a row in the result set
        // In this case, we just join the filterConcept into the original query
        boolean isAttrVarPrimaryKey = this.isConceptQuery(attrQuery, attrVar);
        //isAttrVarPrimaryKey = false;

        Query result;
        if(isAttrVarPrimaryKey) {
            // Case for e.g. Get the number of products offered by vendors in Europe
            // Select ?vendor Count(Distinct ?product) { ... }

            result = attrQuery.cloneQuery();

            Element se;
            if(forceSubQuery) {

                // Select ?s { attrElement(?s, ?x) filterElement(?s) }
                Query sq = new Query();
                sq.setQuerySelectType();
                sq.setDistinct(true);
                sq.getProject().add(attrConcept.getVar());
                sq.setQueryPattern(attrQuery.getQueryPattern());

                Element tmp = new ElementSubQuery(sq);

                Set<Var> refVars = attrQuery.getProject().getRefVars();
                if(refVars.size() == 1 && attrVar.equals(refVars.iterator().next())) {
                    se = tmp;
                } else {
                    ElementGroup foo = new ElementGroup();
                    foo.addElement(attrQuery.getQueryPattern());
                    foo.addElement(tmp);
                    se = foo;
                }

            } else {
                se = attrQuery.getQueryPattern();
            }


            if(!renamedFilterConcept.isSubjectConcept()) {
                Element newElement = new ElementGroup([se, renamedFilterConcept.getElement()]);
                //newElement = newElement.flatten();
                result.setQueryPattern(newElement);
            }

            result.setLimit(limit);
            result.setOffset(offset);
        } else {
            // Case for e.g. Get all products offered by some 10 vendors
            // Select ?vendor ?product { ... }

            boolean requireSubQuery = limit != null || offset != null;


            Element newFilterElement;
            if(requireSubQuery) {
                Concept subConcept;
                if(isLeftJoin) {
                    subConcept = renamedFilterConcept;
                } else {
                    // If we do an inner join, we need to include the attrQuery's element in the sub query

                    Element subElement;
                    if(renamedFilterConcept.isSubjectConcept()) {
                        subElement = attrQuery.getQueryPattern();
                    } else {
                        subElement = new ElementGroup([attrQuery.getQueryPattern(), renamedFilterConcept.getElement()]);
                    }

                    subConcept = new Concept(subElement, attrVar);
                }

                Query subQuery = ConceptUtils.createQueryList(subConcept, limit, offset);
                newFilterElement = new ElementSubQuery(subQuery);
            }
            else {
                newFilterElement = renamedFilterConcept.getElement();
            }

//            var canOptimize = isAttrVarPrimaryKey && requireSubQuery && !isLeftJoin;
//
//            var result;
//
//            //console.log('Optimize: ', canOptimize, isAttrConceptQuery, requireSubQuery, isLeftJoin);
//            if(canOptimize) {
//                // Optimization: If we have a subQuery and the attrQuery's projection is only 'DISTINCT ?attrVar',
//                // then the subQuery is already the result
//                result = newFilterElement.getQuery();
//            } else {


            Query query = attrQuery.clone();

            Element attrElement = query.getQueryPattern();

            Element newAttrElement;
            if(!requireSubQuery && (filterConcept != null || filterConcept.isSubjectConcept())) {
                newAttrElement = attrElement;
            }
            else {
                if(isLeftJoin) {
                    newAttrElement = new ElementGroup([
                        newFilterElement,
                        new ElementOptional(attrElement)
                    ]);
                } else {
                    newAttrElement = new ElementGroup([
                        attrElement,
                        newFilterElement
                    ]);
                }
            }

            query.setQueryPattern(newAttrElement);
            result = query;
        }

        // console.log('Argh Query: ' + result, limit, offset);
        return result;
    },

}
