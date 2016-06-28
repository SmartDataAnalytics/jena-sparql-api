package org.aksw.jena_sparql_api.shape.algebra.op;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptOps;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.vocabulary.RDF;

public class OpVisitorSparql
    implements OpVisitor<Concept>
{
    protected PathExVisitorSparql pathVisitor;
    protected Generator<Var> generator;

    public OpVisitorSparql(Generator<Var> generator) {
        this.generator = generator;
    }

    @Override
    public Concept visit(OpAssign op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Concept visit(OpAnd op) {
        Concept a = op.getLeft().accept(this);
        Concept b = op.getRight().accept(this);
        Concept result = ConceptOps.intersect(a, b, generator);
        return result;
    }

    @Override
    public Concept visit(OpUnion op) {
        Concept a = op.getLeft().accept(this);
        Concept b = op.getRight().accept(this);
        Concept result = ConceptOps.union(a, b, generator);
        return result;
    }

    @Override
    public Concept visit(OpExists op) {
        Relation relation = op.getRole();
        Concept filler = op.getSubOp().accept(this);
        Concept result = ConceptOps.exists(relation, filler, generator);
        return result;
    }

    @Override
    public Concept visit(OpForAll op) {
        Relation relation = op.getRole();
        Concept filler = op.getSubOp().accept(this);
        Concept result = ConceptOps.forAllIfRolePresent(relation, filler, generator);
        return result;
    }

    @Override
    public Concept visit(OpSparqlConcept op) {
        Concept result = op.getConcept();
        return result;
    }

    @Override
    public Concept visit(OpType op) {
        Node node = op.getType();
        Element e = ElementUtils.createElement(new Triple(Vars.s, RDF.type.asNode(), node));
        Concept result = new Concept(e, Vars.s);
        return result;
    }

    @Override
    public Concept visit(OpTop op) {
        Concept result = Concept.TOP;//ConceptUtils.createSubjectConcept();
        return result;
    }

    @Override
    public Concept visit(OpConcept op) {
        Concept result = op.getConcept();
        return result;
    }

    @Override
    public Concept visit(OpFilter op) {
        Expr expr = op.getExpr();
        Concept concept = op.getSubOp().accept(this);
        Var conceptVar = concept.getVar();
        Map<Var, Var> varMap = Collections.singletonMap(Vars._, conceptVar);

        Expr newExpr = ExprUtils.applyNodeTransform(expr, varMap);
        Element newElement = ElementUtils.mergeElements(concept.getElement(), new ElementFilter(newExpr));

        Concept result = new Concept(newElement, conceptVar);
        return result;
    }

    @Override
    public Concept visit(OpFocus op) {
        Concept concept = op.getSubOp().accept(this);
        Path path = op.getPath();
        Relation relation = Relation.create(path);
        Concept result = ConceptUtils.getRelatedConcept(concept, relation);
        return result;
    }

    @Override
    public Concept visit(OpEnumeration op) {
        List<Node> nodes = op.getNodes();

        ElementData data = new ElementData();
        data.add(Vars.s);
        for(Node node : nodes) {
            BindingHashMap binding = new BindingHashMap();
            binding.add(Vars.s, node);
            data.add(binding);
        }

        Concept result = new Concept(data, Vars.s);
        return result;
    }

}
