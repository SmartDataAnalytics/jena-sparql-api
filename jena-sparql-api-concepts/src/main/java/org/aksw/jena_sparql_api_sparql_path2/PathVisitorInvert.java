package org.aksw.jena_sparql_api_sparql_path2;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;


/**
 * Apply an invertion on a given path and stores the obtained path in 'result'
 *
 * @author raven
 *
 */
public class PathVisitorInvert
    implements PathVisitorRewrite
{
    public static Path apply(Path path) {
        PathVisitorInvert visitor = new PathVisitorInvert();
        path.visit(visitor);
        Path result = visitor.getResult();
        return result;
    }

    protected Path result = null;

    public Path getResult() {
        return result;
    }

    @Override
    public void visit(P_Link path) {
        result = new P_ReverseLink(path.getNode());

    }

    @Override
    public void visit(P_ReverseLink path) {
        result = new P_Link(path.getNode());
    }

    @Override
    public void visit(P_NegPropSet path) {
        P_NegPropSet tmp = new P_NegPropSet();
        for(Node node : path.getFwdNodes()) {
            tmp.add(new P_ReverseLink(node));
        }

        for(Node node : path.getBwdNodes()) {
            tmp.add(new P_Link(node));
        }
        result = tmp;
    }

    @Override
    public void visit(P_Inverse path) {
        result = path.getSubPath();
    }

    @Override
    public void visit(P_Mod path) {
        result = new P_Mod(new P_Inverse(path.getSubPath()), path.getMin(), path.getMax());

    }

    @Override
    public void visit(P_FixedLength path) {
        result = new P_FixedLength(new P_Inverse(path.getSubPath()), path.getCount());

    }

    @Override
    public void visit(P_Distinct path) {
        result = new P_Distinct(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_Multi path) {
        result = new P_Multi(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_Shortest path) {
        result = new P_Shortest(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_ZeroOrOne path) {
        result = new P_ZeroOrOne(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        result = new P_ZeroOrMore1(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        result = new P_ZeroOrMoreN(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_OneOrMore1 path) {
        result = new P_OneOrMore1(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_OneOrMoreN path) {
        result = new P_OneOrMoreN(new P_Inverse(path.getSubPath()));

    }

    @Override
    public void visit(P_Alt path) {
        result = new P_Alt(new P_Inverse(path.getLeft()), new P_Inverse(path.getRight()));

    }

    @Override
    public void visit(P_Seq path) {
        result = new P_Alt(new P_Inverse(path.getLeft()), new P_Inverse(path.getRight()));

    }
}


