package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathTransformCopyBase;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;

public class PathTransformerNullFold
	extends PathTransformCopyBase
{
	protected boolean isNull(Path path) {
		boolean result = PathUtils.isNull(path);
		return result;
	}
	
//	public static <T> Path transform(Path left, Path right, Supplier<Path> fn) {
//		Path result = isNull(left)
//				? right
//				: (isNull(right)
//						? left
//						: fn.get());
//
//		return result;
//	}

	@Override
	public Path transform(P_Mod path, Path subPath, long min, long max) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath, min, max);
		return result;
	}

	@Override
	public Path transform(P_Multi path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_Shortest path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_ZeroOrOne path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_ZeroOrMore1 path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_ZeroOrMoreN path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_Seq path, Path left, Path right) {
		Path result = isNull(left)
				? left
				: (isNull(right)
						? right
						: super.transform(path, left, right));
		return result;
	}
	
	@Override
	public Path transform(P_Alt path, Path left, Path right) {
		Path result = isNull(left)
				? right
				: (isNull(right)
						? left
						: super.transform(path, left, right));

		return result;
	}

	@Override
	public Path transform(P_Distinct path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_OneOrMore1 path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_OneOrMoreN path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	@Override
	public Path transform(P_Inverse path, Path subPath) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath);
		return result;
	}

	
	@Override
	public Path transform(P_FixedLength path, Path subPath, long count) {
		Path result = isNull(subPath) ? subPath : super.transform(path, subPath, count);
		return result;
	}

}
