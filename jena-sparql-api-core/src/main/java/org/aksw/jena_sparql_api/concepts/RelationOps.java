package org.aksw.jena_sparql_api.concepts;

import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementSubQuery;


public class RelationOps {

	
	
    public static BinaryRelation from(org.apache.jena.sparql.path.Path path) {
        TriplePath tp = new TriplePath(Vars.s, path, Vars.o);
        ElementPathBlock e = new ElementPathBlock();
        e.addTriplePath(tp);
        BinaryRelation result = new BinaryRelationImpl(e, Vars.s, Vars.o);
        return result;
    }

    /**
     * Takes a sparql relation and returns a new one which only links an s with a t
     * if all t's are the same.
     * Example:
     * All teams whose members were all born in the same country:
     *
     * x, y: x forAllHavingTheSameValue(hasMembers/birthPlace/country) y
     *
     *
     * @param relation
     * @return
     */
    public static BinaryRelation forAllHavingTheSameValue(BinaryRelation role, Generator<Var> baseGenerator) {
//      Select ?x Count(?c) As ?cnt1{ ?a hasPartner/inCountry ?x } Group By ?s
//      Select ?s ?x (Count(?c) As ?cnt2){ ?a hasPartner/inCountry ?x } Group By ?s ?x
//      Filter(?cnt1 = ?cnt2)

        Set<Var> blacklist = role.getVarsMentioned();
        Generator<Var> generator = VarGeneratorBlacklist.create(baseGenerator, blacklist);

        Var cnta = generator.next();
        Var cntb = generator.next();

        Var sourceVar = role.getSourceVar();
        Var targetVar = role.getTargetVar();
        Element roleElement = role.getElement();
        ExprVar targetEv = new ExprVar(targetVar);

        Query qa = new Query();
        qa.setQuerySelectType();
        VarExprList pa = qa.getProject();
        pa.add(sourceVar);
        pa.add(cnta, new ExprAggregator(Vars.x, new AggCountVar(targetEv)));
        qa.setQueryPattern(roleElement);
        qa.addGroupBy(sourceVar);

        Query qb = new Query();
        qb.setQuerySelectType();
        VarExprList pb = qb.getProject();
        pb.add(sourceVar);
        pb.add(targetVar);
        pb.add(cntb, new ExprAggregator(Vars.y, new AggCountVar(targetEv)));

        // rename variables of the concept to make them different from the role

        qb.setQueryPattern(role.getElement());
        qb.addGroupBy(sourceVar);
        qb.addGroupBy(targetVar);


        ElementGroup e = new ElementGroup();
        e.addElement(new ElementSubQuery(qa));
        e.addElement(new ElementSubQuery(qb));
        e.addElement(new ElementFilter(new E_Equals(new ExprVar(cnta), new ExprVar(cntb))));

        BinaryRelation result = new BinaryRelationImpl(e, sourceVar, targetVar);
        return result;
    }
}
