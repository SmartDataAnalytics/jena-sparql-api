package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import java.util.List;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;

import com.google.common.collect.Streams;

public class PathUtils {
	
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

}
