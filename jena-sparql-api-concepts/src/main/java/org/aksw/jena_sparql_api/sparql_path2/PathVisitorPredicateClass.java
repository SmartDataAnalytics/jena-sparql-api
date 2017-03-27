package org.aksw.jena_sparql_api.sparql_path2;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitorBase;

public class PathVisitorPredicateClass
    extends PathVisitorBase
{
    protected PredicateClass result;

    public PathVisitorPredicateClass() {
        super();
    }

    public PredicateClass getResult() {
        return result;
    }

    public static PredicateClass toPredicateClass(P_NegPropSet path) {
        // If the set of nodes is empty, the valueset gets positive polarity, otherwise its negative
        ValueSet<Node> f = ValueSet.create(path.getFwdNodes().isEmpty(), path.getFwdNodes());
        ValueSet<Node> b = ValueSet.create(path.getBwdNodes().isEmpty(), path.getBwdNodes());

        PredicateClass result = new PredicateClass(f, b);
        return result;
    }

    public static PredicateClass toPredicateClass(P_Path0 path) {
        ValueSet<Node> f;
        ValueSet<Node> b;
        if(path.isForward()) {
            f = ValueSet.create(true, path.getNode());
            b = ValueSet.create(true);
        } else {
            f = ValueSet.create(true);
            b = ValueSet.create(true, path.getNode());
        }

        PredicateClass result = new PredicateClass(f, b);
        return result;
    }

    @Override
    public void visit(P_NegPropSet path) {
        result = toPredicateClass(path);
    }

    @Override
    public void visit(P_ReverseLink path) {
        result = toPredicateClass(path);
    }

    @Override
    public void visit(P_Link path) {
        result = toPredicateClass(path);
    }

    public static PredicateClass transform(Path path) {
        PathVisitorPredicateClass visitor = new PathVisitorPredicateClass();
        path.visit(visitor);
        PredicateClass result = visitor.getResult();
        return result;
    }
}
