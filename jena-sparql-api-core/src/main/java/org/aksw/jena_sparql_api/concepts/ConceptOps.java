package org.aksw.jena_sparql_api.concepts;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;

public class ConceptOps {


    public static Concept forAllIfRolePresent(Relation role, Concept filler, Generator<Var> generator) {

        Concept result;
        if(filler == Concept.TOP) {
            result = exists(role, filler, generator);
        } else {
            // Note: counts are assigned to the variables Vars.a and Vars.b

            Var sourceVar = role.getSourceVar();
            Var targetVar = role.getTargetVar();
            Var cnta = generator.next();
            Var cntb = generator.next();
            Element roleElement = role.getElement();
            ExprVar targetEv = new ExprVar(targetVar);

            Query qa = new Query();
            VarExprList pa = qa.getProject();
            pa.add(sourceVar);
            pa.add(cnta, new ExprAggregator(Vars.x, new AggCountVar(targetEv)));
            qa.setQueryPattern(roleElement);
            qa.addGroupBy(sourceVar);

            Query qb = new Query();
            VarExprList pb = qb.getProject();
            pb.add(sourceVar);
            pb.add(cntb, new ExprAggregator(Vars.y, new AggCountVar(targetEv)));

            // rename variables of the concept to make them different from the role
            Concept aligned = align(role.getTargetConcept(), filler, generator);
            Element x = ElementUtils.mergeElements(roleElement, aligned.getElement());

            qb.setQueryPattern(x);
            qb.addGroupBy(sourceVar);


            ElementGroup e = new ElementGroup();
            e.addElement(new ElementSubQuery(qa));
            e.addElement(new ElementSubQuery(qb));
            e.addElement(new ElementFilter(new E_Equals(new ExprVar(cnta), new ExprVar(cntb))));

            result = new Concept(e, sourceVar);
            //ElementUtils.merg
        }

        return result;
    }

//    public static Concept exists(Path path, Concept filler) {
//        Relation relation = RelationOps.createRelation(path, false);
//        Concept result = exists(relation, filler);
//        return result;
//    }

    public static Concept exists(Relation relation, Concept filler, Generator<Var> generator) {
        Concept aligned = align(relation.getTargetConcept(), filler, generator);
        Element x = ElementUtils.mergeElements(relation.getElement(), aligned.getElement());
        Concept result = new Concept(x, relation.getSourceVar());
        return result;

        // Rename variables of the concept to make them distinct from those of the relation
    }



    public static Concept align(Concept concept, Set<Var> vbs, Var vbJoinVar, Generator<Var> generator) {
        Set<Var> vas = concept.getVarsMentioned();
        Map<Var, Var> varMap = VarUtils.createDistinctVarMap(vas, vbs, true, generator);

        varMap.put(vbJoinVar, concept.getVar());
        NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);

        Concept result = concept.applyNodeTransform(nodeTransform);
        return result;
    }

    public static Concept align(Concept base, Concept modifyee, Generator<Var> generator) {
        Set<Var> vbs = modifyee.getVarsMentioned();
        Concept result = align(base, vbs, modifyee.getVar(), generator);
        return result;
    }

    public static Concept union(Stream<Concept> conceptStream) {
        Concept result = conceptStream.reduce(Concept.BOTTOM, (x, y) -> ConceptOps.union(x, y, null));
        return result;
    }

    public static Concept intersect(Stream<Concept> conceptStream) {
        Concept result = conceptStream.reduce(Concept.TOP, (x, y) -> ConceptOps.union(x, y, null));
        return result;
    }

    public static Concept union(Concept concept, Concept filter, Generator<Var> generator) {
        Concept result;

        if(filter != null && filter != Concept.BOTTOM) {
            Concept tmp = align(concept, filter, generator);
            Element e = ElementUtils.unionElements(concept.getElement(), tmp.getElement());
            result = new Concept(e, concept.getVar());
      } else {
          result = concept;
      }

      return result;

    }

    public static Concept intersect(Concept concept, Concept filter) {

        Concept result;

        if(filter != null && !filter.isSubjectConcept() && filter != Concept.TOP) {
//            Set<Var> vas = concept.getVarsMentioned();
//            Set<Var> vbs = filter.getVarsMentioned();
//            Generator<Var> generator = VarGeneratorImpl.create("v");
//            Map<Var, Var> varMap = VarUtils.createDistinctVarMap(vas, vbs, true, generator);
//
//            //if(!filter.getVar().equals(concept.getVar())) {
//            varMap.put(filter.getVar(), concept.getVar());
//            //}
//
//            NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);
//
//            Concept tmp = filter.applyNodeTransform(nodeTransform);

            Concept tmp = align(concept, filter, null);
            Element e = ElementUtils.mergeElements(concept.getElement(), tmp.getElement());
            result = new Concept(e, concept.getVar());
        } else {
            result = concept;
        }

        return result;
    }
}
