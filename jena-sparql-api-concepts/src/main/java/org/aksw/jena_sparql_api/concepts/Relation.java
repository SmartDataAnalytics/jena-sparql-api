package org.aksw.jena_sparql_api.concepts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

import com.google.common.collect.Sets;

/**
 * A (SPARQL) relation is a unifying abstraction for queries and graph patterns which allows treating both as tables.
 * Variables can be marked as "distinguished" which means that they will be projected when generating a relation's
 * effective SPARQL query.
 * Furthermore, the relation inferface declares several methods for constructing joins between relations.
 *
 *
 * @author raven Mar 7, 2018
 *
 */
public interface Relation
    extends HasElement
{

    /**
     * Return the distinguished variables of the relation.
     * The returned list is should be duplicate-free.
     * The variables are NOT required to occur in the relation's element.
     *
     * @return A list of variables
     */
    List<Var> getVars();



    /**
     * Return the set of mentioned variables without the distinguished ones
     *
     * @return
     */
    default Set<Var> getNonDistinguishedVars() {
        Set<Var> mentionedVars = getVarsMentioned();
        Set<Var> markedVars = new HashSet<>(getVars());
        Set<Var> result = Sets.difference(mentionedVars, markedVars);

        return result;
    }


    default Relation rename(Function<String, String> renameFn, Var ... constantVars) {

        Collection<Var> constants = new HashSet<>(Arrays.asList(constantVars));
        Set<Var> vars = getVarsMentioned();
        Map<Var, Var> oldToNew = vars.stream()
            .filter(v -> !constants.contains(v))
            .collect(Collectors.toMap(v -> v, v -> Var.alloc(renameFn.apply(v.getName()))));

        Element newElement = ElementUtils.createRenamedElement(getElement(), oldToNew);
        List<Var> newVars = getVars().stream().map(v -> oldToNew.getOrDefault(v, v)).collect(Collectors.toList());

        Relation result = new RelationImpl(newElement, newVars);
        return result;
    }

    /**
     * Rename the variables of the relation to the given variables
     * In case of clashes, prior variables will be replaced with fresh ones.
     * Delegates ot {@link RelationUtils#rename(Relation, List)}.
     *
     * @param r
     * @param targetVars
     * @return
     */
    default Relation rename(List<Var> targetVars) {
        return RelationUtils.rename(this, targetVars);
    }

    default UnaryRelation toUnaryRelation() {
        List<Var> vars = getVars();
        UnaryRelation result;
        if(vars.size() == 1) {
            Element e = getElement();
            result = new Concept(e, vars.get(0));
        } else {
            throw new UnsupportedOperationException();
        }

        return result;
    }

    default BinaryRelation toBinaryRelation() {
        List<Var> vars = getVars();
        BinaryRelation result;
        if(vars.size() == 2) {
            Element e = getElement();
            result = new BinaryRelationImpl(e, vars.get(0), vars.get(1));
        } else {
            throw new UnsupportedOperationException();
        }

        return result;
    }


    default TernaryRelation toTernaryRelation() {
        List<Var> vars = getVars();
        TernaryRelation result;
        if(vars.size() == 3) {
            Element e = getElement();
            result = new TernaryRelationImpl(e, vars.get(0), vars.get(1), vars.get(2));
        } else {
            throw new UnsupportedOperationException();
        }

        return result;
    }

//	default TernaryRelation asTernaryRelation() {
//
//	}


    default Set<Var> getVarsMentioned() {
        //Element e = getElement();

        // We cannot use PatternVars here, as this only returns the visible vars -
        // which can break global substitutions -
        // e.g. ?p is not visible in SELECT ?s ?o { ?s ?p ?o } - so if we worngly think ?p is a free
        // variable name and substituted ?s with ?p, we would alter the query to SELECT ?s ?o { ?p ?p ?o }
        //Set<Var> result = SetUtils.asSet(PatternVars.vars(e));

        //ElementTransformer
//		NodeTransformCollectNodes tmp = new NodeTransformCollectNodes();
//		this.applyNodeTransform(tmp);
//		Set<Node> nodes = tmp.getNodes();
//		Set<Var> result = nodes.stream()
//				.filter(Node::isVariable)
//				.map(n -> (Var)n)
//				.collect(Collectors.toSet());

        Element e = getElement();
        Set<Var> result = ElementUtils.getVarsMentioned(e);
        List<Var> distinguishedVars = getVars();
        result.addAll(distinguishedVars);

//		Op op = Algebra.compile(e);
//		Collection<Var> tmp = OpVars.mentionedVars(op);

//		Set<Var> result = SetUtils.asSet(tmp);

//		Set<Var> result = new HashSet<>(PatternVars.vars(e));
//
//		// Note: Usually the relation can be considered inconsistent if the vars are not
//		// mentioned in element; however, it is useful for empty relations
//		result.addAll(getVars());
        return result;
    }

    default Relation applyNodeTransform(NodeTransform nodeTransform) {
        return applyDefaultNodeTransform(this, nodeTransform);
    }

    // TODO Move to relation utils
    static Relation applyDefaultNodeTransform(Relation r, NodeTransform nodeTransform) {
        List<Var> transformedVars = r.getVars().stream()
                .map(v -> VarUtils.applyNodeTransform(v, nodeTransform))
                .collect(Collectors.toList());

        Element transformedElement = ElementUtils.applyNodeTransform(r.getElement(), nodeTransform);

        Relation result = new RelationImpl(transformedElement, transformedVars);
        return result;
    }


    // Keeps all variables of this relation intact, and appends the element of another relation
    // TODO Better rename to appendOn(...) - join is misleading, as we are talking about a
    // syntactic transformation - which usually - but not always - corresponds to a join
    default RelationJoiner joinOn(List<Var> vars) {
        return new RelationJoiner(this, vars);
    }

    default RelationJoiner joinOn(Var ... vars) {
        return RelationJoiner.from(this, vars);
    }

    // Similar to joinOn - but *prepends* the elements of the other relation
    default RelationJoiner prependOn(Var ... vars) {
        return RelationJoiner.from(this, vars).filterRelationFirst(true);
    }

    default Relation project(List<Var> vars) {
        return new RelationImpl(getElement(), vars);
    }

    default Relation project(Var ... vars) {
        return project(Arrays.asList(vars));
    }


    default Relation filter(Expr ... exprs) {
        return filter(Arrays.asList(exprs));
    }

    default Relation filter(Iterable<Expr> exprs) {
        Relation result = !exprs.iterator().hasNext()
            ? this
            : filter(ExprUtils.andifyBalanced(exprs));

        return result;
    }


    default Relation filter(Expr expr) {
        List<Element> elts = new ArrayList<>();
        elts.addAll(getElements());
        elts.add(new ElementFilter(expr));
        Element newElt = ElementUtils.groupIfNeeded(elts);

        List<Var> vars = getVars();
        Relation result = new RelationImpl(newElt, vars);
        return result;

    }

    default Query toQuery() {
        Query result = RelationUtils.createQuery(this);
        return result;

//        Query result = new Query();
//        result.setQuerySelectType();
//
//        Element e = getElement();
//        List<Var> vars = getVars();
//        result.setQueryPattern(e);
//
//        VarExprList project = result.getProject();
//        vars.forEach(project::add);
//
//        return result;
    }

    default List<Element> getElements() {
        return ElementUtils.toElementList(getElement());
    }



//	public static TernaryRelation from(Triple t) {
//		new TernaryRelationImpl(ElementUtils.createElement(t), t.getSubject(), t.getPredicate(), t.getObject())
//	}
}
