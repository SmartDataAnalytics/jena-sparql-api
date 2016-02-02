package org.aksw.jena_sparql_api_sparql_path2;

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

public interface PathTransform
{
    Path transform(P_Link path, Node node);
    Path transform(P_ReverseLink path, Node node);
    Path transform(P_NegPropSet path, List<Node> fwdNodes, List<Node> bwdNodes);
    Path transform(P_Inverse path, Path subPath);
    Path transform(P_Mod path, Path subPath, long min, long max);
    Path transform(P_FixedLength path, Path subPath, long count);
    Path transform(P_Distinct path, Path subPath);
    Path transform(P_Multi path, Path subPath);
    Path transform(P_Shortest path, Path subPath);
    Path transform(P_ZeroOrOne path, Path subPath);
    Path transform(P_ZeroOrMore1 path, Path subPath);
    Path transform(P_ZeroOrMoreN path, Path subPath);
    Path transform(P_OneOrMore1 path, Path subPath);
    Path transform(P_OneOrMoreN path, Path subPath);
    Path transform(P_Alt path, Path left, Path right);
    Path transform(P_Seq path, Path left, Path right);
}