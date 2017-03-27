package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import java.util.List;

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

public class PathTransformCopyBase
    implements PathTransform
{
    @Override
    public Path transform(P_Link path, Node node) {
        Path result = path.getNode() == node
                ? path
                : new P_Link(node)
                ;
        return result;
    }

    @Override
    public Path transform(P_ReverseLink path, Node node) {
        Path result = path.getNode() == node
                ? path
                : new P_ReverseLink(node)
                ;
        return result;
    }

    public static P_NegPropSet createNegPropSet(List<Node> fwdNodes, List<Node> bwdNodes) {
        P_NegPropSet result = new P_NegPropSet();
        for(Node node : fwdNodes) {
            result.add(new P_Link(node));
        }

        for(Node node : bwdNodes) {
            result.add(new P_ReverseLink(node));
        }

        return result;
    }

    @Override
    public Path transform(P_NegPropSet path, List<Node> fwdNodes, List<Node> bwdNodes) {
        Path result = path.getFwdNodes() == fwdNodes && path.getBwdNodes() == bwdNodes
                ? path
                : createNegPropSet(fwdNodes, bwdNodes)
                ;
        return result;
    }

    @Override
    public Path transform(P_Inverse path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_Inverse(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_Mod path, Path subPath, long min, long max) {
        Path result = path.getSubPath() == subPath && path.getMin() == min && path.getMax() == max
                ? path
                : new P_Mod(subPath, min, max)
                ;
        return result;
    }

    @Override
    public Path transform(P_FixedLength path, Path subPath, long count) {
        Path result = path.getSubPath() == subPath && path.getCount() == count
                ? path
                : new P_FixedLength(subPath, count)
                ;
        return result;
    }

    @Override
    public Path transform(P_Distinct path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_Distinct(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_Multi path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_Multi(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_Shortest path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_Shortest(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_ZeroOrOne path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_ZeroOrOne(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_ZeroOrMore1 path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_ZeroOrMore1(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_ZeroOrMoreN path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_ZeroOrMoreN(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_OneOrMore1 path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_OneOrMore1(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_OneOrMoreN path, Path subPath) {
        Path result = path.getSubPath() == subPath
                ? path
                : new P_OneOrMoreN(subPath)
                ;
        return result;
    }

    @Override
    public Path transform(P_Alt path, Path left, Path right) {
        Path result = path.getLeft() == left && path.getRight() == right
                ? path
                : new P_Alt(left, right)
                ;
        return result;
    }

    @Override
    public Path transform(P_Seq path, Path left, Path right) {
        Path result = path.getLeft() == left && path.getRight() == right
                ? path
                : new P_Seq(left, right)
                ;
        return result;

    }
}