package org.aksw.jena_sparql_api.util.tuple;

import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;

public class TupleUtils {

    /**
     * Create a binding from two tuples, the first acting as a pattern and the second as the source of values for
     * assignment to the pattern's variables.
     * In essence, the resulting binding is created by pairing up the components of the tuples:
     * binding.add(pattern[i], assigment[i]).
     * If a concrete node of the pattern is paired with a non-equivalent node then null is returned.
     *
     * @param <T> The tuple type.
     * @param accessor The tuple accessor (components must be Nodes)
     * @param pattern The tuple pattern (may contain variables)
     * @param assignment The tuple used to assign values to the variables (should not contain variables)
     * @return A binding or null.
     */
    public static <T> Binding tupleToBinding(TupleAccessor<T, Node> accessor, T pattern, T assignment) {
        BindingBuilder builder = BindingFactory.builder();
        for (int i = 0; i < accessor.getDimension(); ++i) {
            Node nodeOrVar = accessor.get(pattern, i);
            Node node = accessor.get(assignment, i);

            if (NodeUtils.isNullOrAny(node)) {
                // nothing to do
            } else if (nodeOrVar.isVariable()) {
                builder.add((Var)nodeOrVar, node);
            } else {
                if (!nodeOrVar.equals(node)) {
                    builder = null;
                    break;
                }
            }
        }

        Binding result = builder == null ? null : builder.build();
        return result;
    }
}
