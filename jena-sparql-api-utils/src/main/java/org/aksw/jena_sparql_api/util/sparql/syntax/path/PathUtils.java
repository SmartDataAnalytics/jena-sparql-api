package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.util.NodeUtils;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Streams;

public class PathUtils {

    public static final Path nullPath = new P_Link(org.aksw.jena_sparql_api.utils.NodeUtils.nullUriNode);


    /** Return the set of nodes mentioned in a path */
    public static Set<Node> collectNodes(Path path) {
        NodeTransformCollectNodes nodeTransform = new NodeTransformCollectNodes();
        PathTransformer.transform(path, nodeTransform);
        Set<Node> result = nodeTransform.getNodes();
        return result;
    }

    public static boolean isNull(Path path) {
        boolean result = nullPath.equals(path);
        return result;

//		boolean result = ExprEvalValueSet.tryCastAs(P_Path0.class, path)
//				.filter(p -> NULL.equals(p))
//				//.filter(p -> Node.NULL.equals(p.getNode()))
//				.isPresent();
//		return result;
    }

    public static Path foldNulls(Path path) {
        Path result = PathTransformer.transform(path, new PathTransformerNullFold());
        return result;
    }

    public static Path canonicalizeReverseLinks(Path path) {
        Path result = PathTransformer.transform(path, new PathTransformCanonicalizeReverseLinks());
        return result;
    }

    public static Path toSparqlPath(List<P_Path0> steps) {
        return ExprUtils.<Path>opifyBalanced(steps, (a, b) -> new P_Seq(a, b));
        //return ExprUtils.opifyBalanced(steps, P_Seq::new);
    }

    public static P_Path0 createStep(String predicate, boolean isFwd) {
        return createStep(NodeFactory.createURI(predicate), isFwd);
    }

    public static P_Path0 createStep(Node predicate, boolean isFwd) {
        P_Path0 result = isFwd ? new P_Link(predicate) : new P_ReverseLink(predicate);
        return result;
    }


    public static Path create(Path path, boolean isFwd) {
        Path result = isFwd ? path : PathFactory.pathInverse(path);
        return result;
    }

    public static List<P_Path0> toList(Path path) {
        Path tmp = canonicalizeReverseLinks(path);

        PathVisitorToList visitor = new PathVisitorToList();
        tmp.visit(visitor);
        List<P_Path0> result = visitor.getResult();
        return result;
    }

    public static int countForwardLinks(Iterable<? extends Path> paths) {
        int result = (int)Streams.stream(paths)
            .filter(p -> p instanceof P_Path0 ? ((P_Path0)p).isForward() : false)
            .count();
        return result;
    }

    public static int countReverseLinks(Iterable<? extends Path> paths) {
        int result = (int)Streams.stream(paths)
                .filter(p -> p instanceof P_Path0 ? !((P_Path0)p).isForward() : false)
                .count();
        return result;
    }

    public static int compareStep(P_Path0 a, P_Path0 b) {
        int result = ComparisonChain.start()
            .compareTrueFirst(a.isForward(), b.isForward())
            .compare(a.getNode(), b.getNode(), NodeUtils::compareRDFTerms)
            .result();
        return result;
    }
}
