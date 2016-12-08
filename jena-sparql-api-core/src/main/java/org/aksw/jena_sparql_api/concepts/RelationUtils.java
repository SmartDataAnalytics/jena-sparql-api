package org.aksw.jena_sparql_api.concepts;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.ExprUtils;

public class RelationUtils {

//    public static Relation createRelationRenamed(Relation prototype, Relation target) {
//        RelationUtils.create
//
//        Set<Var> allowed = new HashSet<Var>(Arrays.asList(sourceVar, targetVar));
//        Set<Var> bl = Sets.difference(blacklist, allowed);
//
//        Generator<Var> gen = VarGeneratorBlacklist.create("v", bl);
//
//        Set<Var> sourceVars = relation.getVarsMentioned();
//        Map<Var, Var> varMap = VarUtils.createJoinVarMap();
//
//    }


    public static Triple extractTriple(Relation relation) {
        Element e = relation.getElement();
        Triple result = ElementUtils.extractTriple(e);
        return result;
    }

    public static Relation createRelation(String propertyUri, boolean isInverse, PrefixMapping prefixMapping) {

        String p = prefixMapping == null ? propertyUri : prefixMapping.expandPrefix(propertyUri);
        Node node = NodeFactory.createURI(p);
        Relation result = createRelation(node, isInverse);
        return result;
    }


    public static Relation createRelation(Node property, boolean isInverse) {
        Expr expr = new E_Equals(new ExprVar(Vars.p), ExprUtils.nodeToExpr(property));
        Relation result = createRelation(expr, isInverse);
        return result;
    }

    public static Relation createRelation(Property property, boolean isInverse) {
        Relation result = createRelation(property.asNode(), isInverse);
        return result;
    }


    public static Relation createRelation(Expr expr, boolean isInverse) {
        Relation result = new Relation(new ElementFilter(expr), Vars.p, Vars.o);
        return result;
    }


//    public static Relation createRelation(StepRelation step) {
//        Relation result = nav(step.getRelation(), step.isInverse());
//        return result;
//    }


    public static Query createQuery(Relation relation) {
        Query result = new Query();
        result.setQuerySelectType();

        Element e = relation.getElement();;
        result.setQueryPattern(e);

        VarExprList project = result.getProject();
        project.add(relation.getSourceVar());
        project.add(relation.getTargetVar());

        return result;
    }
}
