package org.aksw.jena_sparql_api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;

public class LabelUtils {

    public static TernaryRelation createRelationLiteralPreference(LiteralPreference literalPreference) {
        BestLiteralConfig blc = new BestLiteralConfig(literalPreference, Vars.x, Vars.y, Vars.z);
        TernaryRelation result = createRelationPrefLabels(blc);
        return result;
    }


    public static TernaryRelation createRelationPrefLabels(BestLiteralConfig bestLiteralConfig) {

        List<String> prefLangs = bestLiteralConfig.getLangs();
        List<Node> prefPreds = bestLiteralConfig.getPredicates();

        Var s = bestLiteralConfig.getSubjectVar();
        Var p = bestLiteralConfig.getPredicateVar();
        Var o = bestLiteralConfig.getObjectVar();

        Expr labelExpr = new ExprVar(o);

        // Second, create the element
        List<Expr> langTmp = prefLangs.stream().map(lang -> {
            Expr r = new E_LangMatches(new E_Lang(labelExpr), NodeValue.makeString(lang));
            return r;
        }).collect(Collectors.toList());

        // Combine multiple expressions into a single logicalOr expression.
        Expr langConstraint = ExprUtils.orifyBalanced(langTmp);
        Expr propFilter = ExprUtils.oneOf(p, prefPreds);

        ElementGroup els = new ElementGroup();
        els.addTriplePattern(new Triple(s, p, o));
        els.addElementFilter(new ElementFilter(propFilter));
        els.addElementFilter(new ElementFilter(langConstraint));

        //var result = new Concept(langElement, s);
        TernaryRelation result = new TernaryRelationImpl(els, s, p, o);
        return result;
    }

}

