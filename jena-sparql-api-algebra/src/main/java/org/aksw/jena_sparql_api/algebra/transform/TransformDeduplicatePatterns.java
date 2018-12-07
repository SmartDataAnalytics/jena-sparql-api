package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.QuadPattern;

public class TransformDeduplicatePatterns 
	extends TransformCopy
{	
    public static Op transform(Op op) {
        Transform transform = new TransformDeduplicatePatterns();
        Op result = Transformer.transform(transform, op);
        return result;
    }

	public static BasicPattern deduplicate(BasicPattern pattern) {
		return BasicPattern.wrap(new ArrayList<>(new LinkedHashSet<>(pattern.getList())));
	}
	
	public static PathBlock deduplicate(PathBlock pattern) {
		PathBlock result = new PathBlock();
		new LinkedHashSet<>(pattern.getList()).forEach(result::add);
		return result;
	}

	public static QuadPattern deduplicate(QuadPattern pattern) {
		QuadPattern result = new QuadPattern();
		new LinkedHashSet<>(pattern.getList()).forEach(result::add);
		return result;
	}

	@Override
	public Op transform(OpBGP op) {
		BasicPattern before = op.getPattern();
		BasicPattern after = deduplicate(before);
		Op result = after.equals(before) ? op : new OpBGP(after);
		return result;
	}

	@Override
	public Op transform(OpQuadBlock op) {
		QuadPattern before = op.getPattern();
		QuadPattern after = deduplicate(before);
		Op result = after.equals(before) ? op : new OpQuadBlock(after);
		return result;
	}
	
	@Override
	public Op transform(OpQuadPattern op) {
		BasicPattern before = op.getBasicPattern();
		BasicPattern after = deduplicate(before);
		Op result = after.equals(before) ? op : new OpQuadPattern(op.getGraphNode(), after);
		return result;
	}	
}
