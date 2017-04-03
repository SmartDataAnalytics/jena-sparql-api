package org.aksw.jena_sparql_api.concepts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

public class ConceptOps {

    // This referenced method was removed from NodeTransformLib in Jena 3.2.0
    public static List<SortCondition> transform(NodeTransform nodeTransform, List<SortCondition> conditions)
    {
        List<SortCondition> conditions2 = new ArrayList<>() ;
        boolean same = true ;
        for ( SortCondition sc : conditions )
        {
            Expr expr = sc.getExpression() ;
            Expr expr2 = NodeTransformLib.transform(nodeTransform, expr) ;
            if ( expr != expr2 )
                same = false ;
            SortCondition sc2 = new SortCondition(expr2, sc.getDirection()) ;
            conditions2.add(sc2) ;
        }

        if ( same )
            return conditions ;
        return conditions2 ;
    }


    public static OrderedConcept applyOrder(Concept concept, OrderedConcept ordered, Generator<Var> generator) {
        Map<Var, Var> varMap = createAlignedVarMap(concept, ordered.getConcept(), generator);
        Concept tmp = intersect2(concept, ordered.getConcept(), varMap);

        List<SortCondition> orderBy = /*NodeTransformLib.*/transform(new NodeTransformSubst(varMap), ordered.getOrderBy());

        OrderedConcept result = new OrderedConcept(tmp, orderBy);
        return result;
    }


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
            pb.add(cntb, new ExprAggregator(Vars.y, new AggCountVar(targetEv)));

            // rename variables of the concept to make them different from the role
            Concept aligned = align(filler, role.getTargetConcept(), generator);
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
        Concept targetConcept = relation.getTargetConcept();
        Concept aligned = align(filler, targetConcept, generator);
        Element x = ElementUtils.mergeElements(relation.getElement(), aligned.getElement());
        Concept result = new Concept(x, relation.getSourceVar());
        return result;

        // Rename variables of the concept to make them distinct from those of the relation
    }


    public static Concept align(Concept concept, Set<Var> vbs, Var vbJoinVar, Generator<Var> generator) {
        Map<Var, Var> varMap = createAlignedVarMap(concept, vbs, vbJoinVar, generator);
        Concept result = applyNodeTransform(concept, varMap);
        return result;
    }


    public static Concept applyNodeTransform(Concept concept, Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);

        Concept result = concept.applyNodeTransform(nodeTransform);
        return result;
    }

    public static Map<Var, Var> createAlignedVarMap(Concept concept, Set<Var> vbs, Var vbJoinVar, Generator<Var> generator) {
        Set<Var> vas = concept.getVarsMentioned();
        Map<Var, Var> result = VarUtils.createDistinctVarMap(vas, vbs, true, generator);

        //varMap.put(vbJoinVar, concept.getVar());
        result.put(concept.getVar(), vbJoinVar);

        return result;
    }

    public static Map<Var, Var> createAlignedVarMap(Concept concept, Concept forbiddenVars,  Generator<Var> generator) {
        Var vb = forbiddenVars.getVar();
        Set<Var> vbs = forbiddenVars.getVarsMentioned();
        Map<Var, Var> result = createAlignedVarMap(concept, vbs, vb, generator);

        return result;
    }


    public static Concept align(Concept alignee, Concept forbiddenVars, Generator<Var> generator) {
        Set<Var> vbs = forbiddenVars.getVarsMentioned();
        Concept result = align(alignee, vbs, forbiddenVars.getVar(), generator);
        return result;
    }

    public static Concept union(Stream<Concept> conceptStream) {
        // Note: x,y in order to pass in the identity as second arg, and rename its variables
        // rather than that of the provided concept
        Concept result = conceptStream.reduce(Concept.BOTTOM, (x, y) -> ConceptOps.union(y, x, null));
        return result;
    }

    public static Concept intersect(Stream<Concept> conceptStream) {
        Concept result = conceptStream.reduce(Concept.TOP, (x, y) -> ConceptOps.intersect(y, x, (Generator<Var>)null));
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

    public static Concept intersect2(Concept concept, Concept filter, Map<Var, Var> varMap) {
        Concept result;

        if(filter != null) {
            Concept tmp = ConceptOps.applyNodeTransform(filter, varMap);
            Element e = ElementUtils.mergeElements(concept.getElement(), tmp.getElement());
            result = new Concept(e, concept.getVar());
        } else {
            result = concept;
        }

        return result;

    }

    public static Concept intersect(Concept concept, Concept filter, Generator<Var> generator) {

        Concept result;

        //if(filter != null && !filter.isSubjectConcept() && filter != Concept.TOP) {
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

        if(filter != null) {
            Concept tmp = align(filter, concept, generator);
            Element e = ElementUtils.mergeElements(concept.getElement(), tmp.getElement());
            result = new Concept(e, concept.getVar());
        } else {
            result = concept;
        }

        return result;
    }
}
