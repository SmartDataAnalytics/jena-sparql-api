package org.aksw.jena_sparql_api_sparql_path2;

import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransform;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
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
import org.apache.jena.sparql.syntax.Element;



public class PathTransformInvert
    implements PathTransform
{
    @Override
    public Path transform(P_Link path, Node node) {
        Path result = new P_ReverseLink(node);
        return result;
    }

    @Override
    public Path transform(P_ReverseLink path, Node node) {
        Path result = new P_Link(node);
        return result;
    }

    @Override
    public Path transform(P_NegPropSet path, List<Node> fwdNodes, List<Node> bwdNodes) {
        P_NegPropSet result = new P_NegPropSet();
        for(Node node : fwdNodes) {
            result.add(new P_ReverseLink(node));
        }

        for(Node node : bwdNodes) {
            result.add(new P_Link(node));
        }
        return result;
    }

    @Override
    public Path transform(P_Inverse path, Path subPath) {
        return subPath;
    }

    @Override
    public Path transform(P_Mod path, Path subPath, long min, long max) {
        Path result = new P_Mod(new P_Inverse(subPath), min, max);
        return result;
    }

    @Override
    public Path transform(P_FixedLength path, Path subPath, long count) {
        Path result = new P_FixedLength(new P_Inverse(subPath), path.getCount());
        return result;
    }

    @Override
    public Path transform(P_Distinct path, Path subPath) {
        Path result = new P_Distinct(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_Multi path, Path subPath) {
        Path result = new P_Multi(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_Shortest path, Path subPath) {
        Path result = new P_Shortest(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_ZeroOrOne path, Path subPath) {
        Path result = new P_ZeroOrOne(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_ZeroOrMore1 path, Path subPath) {
        Path result = new P_ZeroOrMore1(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_ZeroOrMoreN path, Path subPath) {
        Path result = new P_ZeroOrMoreN(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_OneOrMore1 path, Path subPath) {
        Path result = new P_OneOrMore1(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_OneOrMoreN path, Path subPath) {
        Path result = new P_OneOrMoreN(new P_Inverse(subPath));
        return result;
    }

    @Override
    public Path transform(P_Alt path, Path left, Path right) {
        Path result = new P_Alt(new P_Inverse(left), new P_Inverse(right));
        return result;
    }

    @Override
    public Path transform(P_Seq path, Path left, Path right) {
        Path result = new P_Alt(new P_Inverse(left), new P_Inverse(right));
        return result;
    }
}


