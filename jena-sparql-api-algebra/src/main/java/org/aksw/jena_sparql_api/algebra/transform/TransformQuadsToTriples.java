package org.aksw.jena_sparql_api.algebra.transform;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;

public class TransformQuadsToTriples
    extends TransformCopy
{
    @Override
    public Op transform(OpQuadBlock op) {
        Op result = OpUtils.toOp(op.getPattern().getList(), OpUtils::toOpGraphTriples);
        return result;
    }


    @Override
    public Op transform(OpQuadPattern opQuadPattern) {
        BasicPattern bgp = opQuadPattern.getBasicPattern();
        Node graphNode = opQuadPattern.getGraphNode();

        Op result = OpUtils.toOpGraphTriples(graphNode, bgp);

        return result;
    }
}
