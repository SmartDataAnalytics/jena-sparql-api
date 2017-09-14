package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.commons.graph.index.jena.transform.OpDistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.analysis.DistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpNull;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class NodeMapperOpEquality
    implements TriFunction<Op, Op, TreeMapping<Op, Op, BiMap<Node, Node>, Op>, Entry<BiMap<Node, Node>, Op>>
{
    @Override
    public Entry<BiMap<Node, Node>, Op> apply(Op viewOp, Op userOp, TreeMapping<Op, Op, BiMap<Node, Node>, Op> tm) {
        Class<?> viewOpClass = viewOp.getClass();
        Class<?> userOpClass = userOp.getClass();

        Entry<BiMap<Node, Node>, Op> result;
        if(!Objects.equals(viewOpClass, userOpClass)) {
            result = null;
        } else {
            Class<?> c = viewOpClass;

            if(OpExtConjunctiveQuery.class.isAssignableFrom(c)) {
                result = map((OpExtConjunctiveQuery)viewOp, (OpExtConjunctiveQuery)userOp, tm);
            } else if(OpDistinctExtendFilter.class.isAssignableFrom(c)) {
                result = map((OpDistinctExtendFilter)viewOp, (OpDistinctExtendFilter)userOp, tm);
            } else {
                result = null;
            }

        }

        return result;
    }


    public Entry<BiMap<Node, Node>, Op> map(OpExtConjunctiveQuery viewOp, OpExtConjunctiveQuery userOp, TreeMapping<Op, Op, BiMap<Node, Node>, Op> tm) {
        QuadFilterPatternCanonical view = viewOp.getQfpc().getPattern().applyNodeTransform(new NodeTransformRenameMap(tm.getOverallMatching()));
        QuadFilterPatternCanonical user = userOp.getQfpc().getPattern();

        QuadFilterPatternCanonical residual = user.diff(view);

        Op result = residual.isEmpty()
                ? OpNull.create()
                : null;

        return new SimpleEntry<>(HashBiMap.create(), result);
    }


    public Entry<BiMap<Node, Node>, Op> map(OpDistinctExtendFilter viewOp, OpDistinctExtendFilter userOp, TreeMapping<Op, Op, BiMap<Node, Node>, Op> tm) {
        DistinctExtendFilter view = viewOp.getDef().applyNodeTransform(new NodeTransformRenameMap(tm.getOverallMatching()));
        DistinctExtendFilter user = userOp.getDef();

        // TODO Extensions may yield additional matchings

        Op result = view.equals(user)
                ? OpNull.create()
                : null;

        return new SimpleEntry<>(HashBiMap.create(), result);
    }

}