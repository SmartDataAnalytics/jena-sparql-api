package org.aksw.jena_sparql_api.concepts;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.jena_sparql_api.syntax.QueryGenerationUtils;
import org.aksw.jena_sparql_api.utils.*;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.vocabulary.RDF;

import java.util.*;
import java.util.stream.Collectors;

public class ConceptUtils {
    public static final Concept subjectConcept = createSubjectConcept();

    public static Concept listDeclaredProperties = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || ?t = <http://www.w3.org/2002/07/owl#ObjectProperty> || ?t = <http://www.w3.org/2002/07/owl#DataTypeProperty>)", "s");
    public static Concept listDeclaredClasses = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/2000/01/rdf-schema#Class> || ?t = <http://www.w3.org/2002/07/owl#Class>)", "s");
    public static Concept listUsedClasses = Concept.create("?s a ?t", "t");

    public static Concept listAllPredicates = Concept.create("?s ?p ?o", "p");
    public static Concept listAllGraphs = Concept.create("Graph ?g { ?s ?p ?o }", "g");


    /**
     * Takes a concept and returns a new one that matches the original one's outgoing predicates
     *
     * @return
     */
    public static UnaryRelation createPredicateQuery(UnaryRelation concept) {
        Collection<Var> vars = PatternVars.vars(concept.getElement());
        List<String> varNames = VarUtils.getVarNames(vars);

        Var s = concept.getVar();

        Generator<Var> gen = GeneratorBlacklist.create(VarGeneratorImpl2.create("v"), varNames);
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
    /**
     * True if the concept is isomorph to { ?s ?p ?o }, ?s
     *
     * @return
     */
    public static boolean isSubjectConcept(UnaryRelation r) {
        Element element = r.getElement();
        Var var = r.getVar();

        if(element instanceof ElementTriplesBlock) {
            List<Triple> triples = ((ElementTriplesBlock)element).getPattern().getList();

            if(triples.size() == 1) {

                Triple triple = triples.get(0);

                // TODO Refactor into e.g. ElementUtils.isVarsOnly(element)
                boolean condition =
                        triple.getSubject().isVariable() &&
                        triple.getSubject().equals(var) &&
                        triple.getPredicate().isVariable() &&
                        triple.getObject().isVariable();

                if(condition) {
                    return true;
                }
            }
        }

        return false;
    }



    /**
     * Create a new concept that has no variables with the given one in common
     *
     *
     *
     * @param that
     * @return
     */
    public static UnaryRelation makeDistinctFrom(UnaryRelation a, UnaryRelation that) {

        Set<String> thisVarNames = new HashSet<String>(VarUtils.getVarNames(PatternVars.vars(a.getElement())));
        Set<String> thatVarNames = new HashSet<String>(VarUtils.getVarNames(PatternVars.vars(that.getElement())));

        Set<String> commonVarNames = Sets.intersection(thisVarNames, thatVarNames);
        Set<String> combinedVarNames = Sets.union(thisVarNames, thatVarNames);

        Generator<Var> generator = GeneratorBlacklist.create(VarGeneratorImpl2.create("v"), combinedVarNames);

        BindingBuilder builder = BindingBuilder.create();
        for(String varName : commonVarNames) {
            Var oldVar = Var.alloc(varName);
            Var newVar = Var.alloc(generator.next());

            builder.add(oldVar, newVar);
        }

        Binding binding = builder.build();

        Op op = Algebra.compile(a.getElement());
        Op substOp = Substitute.substitute(op, binding);

        Element newElement;
        if(substOp instanceof OpBGP) {
            BasicPattern bp = ((OpBGP)substOp).getPattern();
            newElement = new ElementTriplesBlock(bp);
        } else {
            Query tmp = OpAsQuery.asQuery(substOp);
            newElement = tmp.getQueryPattern();
        }
        //ElementGroup newElement = new ElementGroup();
        //newElement.addElement(tmp.getQueryPattern());

        /*
        if(newElement instanceof ElementGroup) {


            ElementPathBlock) {
        }
            List<TriplePath> triplePaths = ((ElementPathBlock)newElement).getPattern().getList();

            ElementTriplesBlock block = new ElementTriplesBlock();
            for(TriplePath triplePath : triplePaths) {
                block.addTriple(triplePath.asTriple());
            }

            newElement = block;
            //newElement = new ElementTriplesBlock(pattern);
        }
        */

        Var tmpVar = (Var)binding.get(a.getVar());

        Var newVar = tmpVar != null ? tmpVar : a.getVar();

        Concept result = new Concept(newElement, newVar);
        return result;
    }

    public static Concept createConcept(Node ... nodes) {
        Concept result = createConcept(Arrays.asList(nodes));
        return result;
    }

    public static UnaryRelation createConceptFromRdfNodes(Iterable<? extends RDFNode> rdfNodes) {
        Iterable<Node> nodes = Streams.stream(rdfNodes).map(RDFNode::asNode).collect(Collectors.toList());
        UnaryRelation result = ConceptUtils.createConcept(nodes);
        return result;
    }

    public static Concept createConcept(Iterable<? extends Node> nodes) {
        ElementData data = new ElementData();
        data.add(Vars.s);
        BindingBuilder builder = BindingBuilder.create();
        for(Node node : nodes) {
            builder.reset();
            builder.add(Vars.s, node);
            data.add(builder.build());
        }

        Concept result = new Concept(data, Vars.s);
        return result;

    }

    public static Concept createFilterConcept(Node ... nodes) {
        Concept result = createFilterConcept(Arrays.asList(nodes));
        return result;
    }


    public static Concept createFilterConcept(Iterable<Node> nodes) {

        int size = Iterables.size(nodes);
        Element el;
        switch(size) {
        case 0:
            el = new ElementFilter(NodeValue.FALSE);
            break;
        case 1:
            Node node = nodes.iterator().next();
            el = new ElementFilter(new E_Equals(new ExprVar(Vars.s), NodeValue.makeNode(node)));
            break;
        default:
            el = new ElementFilter(new E_OneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes)));
            break;
        }

        Concept result = new Concept(el, Vars.s);
        return result;
    }

    public static Concept createRelatedConcept(Collection<Node> nodes, BinaryRelation relation) {
        Var sourceVar = relation.getSourceVar();
        Var targetVar = relation.getTargetVar();
        Element relationEl = relation.getElement();

        ExprVar ev = new ExprVar(sourceVar);
        ExprList el = ExprListUtils.nodesToExprs(nodes);
        ElementFilter filterEl = new ElementFilter(new E_OneOf(ev, el));

        Element resultEl = ElementUtils.mergeElements(relationEl, filterEl);

        Concept result = new Concept(resultEl, targetVar);
        return result;
    }


    public static Concept getRelatedConcept(Concept source, BinaryRelation relation) {
        Concept renamedSource = createRenamedSourceConcept(source, relation);

        Element merged = ElementUtils.mergeElements(renamedSource.getElement(), relation.getElement());

        Var targetVar = relation.getTargetVar();

        Concept result = new Concept(merged, targetVar);
        return result;
    }

    // FIMXE Consolidate with QueryGenerationUtils.createQuryCount
    public static Query createQueryCount(UnaryRelation concept, Var outputVar, Long itemLimit, Long rowLimit) {
        Query subQuery = createQueryList(concept);

        if(rowLimit != null) {
            subQuery.setDistinct(false);
            subQuery.setLimit(rowLimit);

            subQuery = QueryGenerationUtils.wrapAsSubQuery(subQuery, concept.getVar());
            subQuery.setDistinct(true);
        }

        if(itemLimit != null) {
            subQuery.setLimit(itemLimit);
        }

        Element esq = new ElementSubQuery(subQuery);

        Query result = new Query();
        result.setQuerySelectType();

        Expr ea = result.allocAggregate(new AggCount());

        result.getProject().add(outputVar, ea);//new ExprAggregator(concept.getVar(), new AggCount()));
        result.setQueryPattern(esq);

        return result;
    }

    public static Set<Var> getVarsMentioned(UnaryRelation concept) {
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

    public static Concept createForRdfType(String iriStr) {
        return createForRdfType(NodeFactory.createURI(iriStr));
    }

    public static Concept createForRdfType(Node type) {
        Concept result = new Concept(
                ElementUtils.createElementTriple(Vars.s, RDF.Nodes.type, type),
                Vars.s);
        return result;
    }


    public static Map<Var, Var> createDistinctVarMap(Set<Var> workload, Set<Var> blacklist, Generator<Var> generator) {
        //Set<Var> varNames = new HashSet<String>(VarUtils.getVarNames(blacklist));
//        Generator<Var> gen = VarGeneratorBlacklist.create(generator, blacklist);

        Map<Var, Var> result = new HashMap<Var, Var>();
        for(Var var : workload) {
            boolean isBlacklisted = blacklist.contains(var);

            Var t;
            if(isBlacklisted) {
                t = generator.next();
                //t = Var.alloc(name);
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
    public static Generator<Var> createGenerator(Concept concept) {
        Collection<Var> tmp = PatternVars.vars(concept.getElement());
        //List<String> varNames = VarUtils.getVarNames(tmp);

        //Generator base = Gensym.create("v");
        Generator<Var> result = VarGeneratorBlacklist.create("v", tmp);

        return result;
    }

    // Create a fresh var that is not part of the concept
//    public static Var freshVar(Concept concept) {
//        Generator gen = createGenerator(concept);
//        String varName = gen.next();
//        Var result = Var.alloc(varName);
//        return result;
//    }

    public static UnaryRelation renameVar(UnaryRelation concept, Var targetVar) {

        UnaryRelation result;
        if(concept.getVar().equals(targetVar)) {
            // Nothing to do since we are renaming the variable to itself
            result = concept;
        } else {
            // We need to rename the concept's var, thereby we need to rename
            // any occurrences of targetVar
            Set<Var> conceptVars = getVarsMentioned(concept);
            Map<Var, Var> varMap = createDistinctVarMap(conceptVars, Collections.singleton(targetVar), VarGeneratorImpl2.create("v"));
            varMap.put(concept.getVar(), targetVar);
            Element replElement = ElementUtils.createRenamedElement(concept.getElement(), varMap);
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

    /**
     *
     * @param concept The concept subject to renaming such that it can act as a filter on the relation's source variable
     * @param relation The relation; variables will remain unchanged
     * @return
     */
    public static Concept createRenamedSourceConcept(Concept concept, BinaryRelation relation) {
        Concept attrConcept = new Concept(relation.getElement(), relation.getSourceVar());
        Concept result = createRenamedConcept(attrConcept, concept);
        return result;
    }

    public static Concept createRenamedConcept(UnaryRelation concept, Map<Var, Var> varMap) {
        Var newVar = MapUtils.getOrElse(varMap, concept.getVar(), concept.getVar());
        Element newElement = ElementUtils.createRenamedElement(concept.getElement(), varMap);

        Concept result = new Concept(newElement, newVar);

        return result;
    }


    /**
     *
     *
     * @param attrConcept The concept whose attributes will remained unchanged
     * @param filterConcept The concept whose variables will be renamed such that it filters the attrConcept
     * @return
     */
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

            filterConcept = createRenamedConcept(filterConcept, varMap);
        }

        Concept tmpConcept;
        if(renameVars) {
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
        if(!tmpElements.isEmpty()) {

            if(tmpConcept.isSubjectConcept()) {
                e = attrConcept.getElement(); //tmpConcept.getElement();
            } else {

                List<Element> newElements = new ArrayList<Element>();

                if(attrsOptional) {
                    attrElement = new ElementOptional(attrConcept.getElement());
                }
                newElements.add(attrElement);

                if(filterAsSubquery) {
                    tmpElements = Collections.<Element>singletonList(new ElementSubQuery(tmpConcept.asQuery()));
                }


                newElements.addAll(tmpElements);
                //newElements.push.apply(newElements, attrElement);
                //newElements.push.apply(newElements, tmpElements);


                e = ElementUtils.createElementGroup(newElements);
                //xxx e = e.flatten();
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


    public static Query createQueryList(UnaryRelation concept) {
        Query result = createQueryList(concept, null, null);
        return result;
    }

    public static Query createQueryList(OrderedConcept orderedConcept, Range<Long> range) {
        Concept concept = orderedConcept.getConcept();
        Query result = createQueryList(concept, range);

        for(SortCondition sc : orderedConcept.getOrderBy()) {
            result.addOrderBy(sc);
        }

        return result;
    }

    public static Query createQueryList(OrderedConcept orderedConcept, Long limit, Long offset) {
        Concept concept = orderedConcept.getConcept();
        Query result = createQueryList(concept, limit, offset);

        for(SortCondition sc : orderedConcept.getOrderBy()) {
            result.addOrderBy(sc);
        }

        return result;
    }


    public static Query createQueryList(UnaryRelation concept, Range<Long> range) {
        long offset = QueryUtils.rangeToOffset(range);
        long limit = QueryUtils.rangeToLimit(range);

        Query result = createQueryList(concept, limit, offset);
        return result;
    }

    public static Query createQueryList(UnaryRelation concept, Long limit, Long offset) {
        Query result = new Query();
        result.setQuerySelectType();
        result.setDistinct(true);

        result.setLimit(limit == null ? Query.NOLIMIT : limit);
        result.setOffset(offset == null ? Query.NOLIMIT : offset);

        result.getProject().add(concept.getVar());
        Element e = concept.getElement();
        if(e instanceof ElementSubQuery) {
            e = ElementUtils.createElementGroup(e);
        }

        result.setQueryPattern(e);

//        String str = result.toString();
//        System.out.println(str);
        return result;
    }


    public static Query createAttrQuery(Query attrQuery, Var attrVar, boolean isLeftJoin, Concept filterConcept, Long limit, Long offset, boolean forceSubQuery) {

        //filterConcept.getElement()
        // TODO Deal with prefixes...

        Concept attrConcept = new Concept(new ElementSubQuery(attrQuery), attrVar);

        Concept renamedFilterConcept = ConceptUtils.createRenamedConcept(attrConcept, filterConcept);
        //console.log('attrConcept: ' + attrConcept);
        //console.log('filterConcept: ' + filterConcept);
        //console.log('renamedFilterConcept: ' + renamedFilterConcept);

        // Selet Distinct ?ori ?gin? alProj { Select (foo as ?ori ...) { originialElement} }

        // Whether each value for attrVar uniquely identifies a row in the result set
        // In this case, we just join the filterConcept into the original query
        boolean isAttrVarPrimaryKey = isConceptQuery(attrQuery, attrVar);
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

                Set<Var> refVars = VarExprListUtils.getRefVars(attrQuery.getProject());
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

            if(isLeftJoin) {
                se = new ElementOptional(se);
            }

            if(!renamedFilterConcept.isSubjectConcept()) {
                Element newElement = ElementUtils.createElementGroup(renamedFilterConcept.getElement(), se);
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
                        subElement = ElementUtils.createElementGroup(attrQuery.getQueryPattern(), renamedFilterConcept.getElement());
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


            Query query = attrQuery.cloneQuery();

            Element attrElement = query.getQueryPattern();

            Element newAttrElement;
            if(!requireSubQuery && (filterConcept != null && filterConcept.isSubjectConcept())) {
                newAttrElement = attrElement;
            }
            else {
                if(isLeftJoin) {
                    newAttrElement = ElementUtils.createElementGroup(
                        newFilterElement,
                        new ElementOptional(attrElement)
                    );
                } else {
                    newAttrElement = ElementUtils.createElementGroup(
                        attrElement,
                        newFilterElement
                    );
                }
            }

            query.setQueryPattern(newAttrElement);
            result = query;
        }

        // console.log('Argh Query: ' + result, limit, offset);
        return result;
    }

    public static Var freshVar(UnaryRelation concept) {
        Var result = freshVar(concept, null);
        return result;
    }

    public static Var freshVar(UnaryRelation concept, String baseVarName) {
        baseVarName = baseVarName == null ? "c" : baseVarName;

        Set<Var> varsMentioned = concept.getVarsMentioned();

        Generator<Var> varGen = VarUtils.createVarGen(baseVarName, varsMentioned);
        Var result = varGen.next();

        return result;
    }

    public static Concept createRenamedConcept(UnaryRelation concept, Var attrVar) {
        Var newVar = freshVar(concept);
        Map<Var, Var> varMap = new HashMap<>();
        varMap.put(attrVar, newVar);
        varMap.put(concept.getVar(), attrVar);
        Concept result = ConceptUtils.createRenamedConcept(concept, varMap);

//        Concept tmp = createRenamedConcept(concept, Collections.singletonMap(attrVar, newVar));
//        Concept result = ConceptUtils.createRenamedConcept(tmp, Collections.singletonMap(tmp.getVar(), attrVar));

        return result;
    }
}
