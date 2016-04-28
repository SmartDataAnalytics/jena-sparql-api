package org.aksw.jena_sparql_api_sparql_path2;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.Pair;

/**
 * The main reason this class extends pair is to have the list interface
 * which allows iterating the directions with get(0) and get(1)
 *
 * @author raven
 *
 */
public class VertexClass<V>
    extends Pair<ValueSet<V>>
{
    private static final long serialVersionUID = -3939204124201128789L;

    public static <V> VertexClass<V> union(Pair<ValueSet<V>> a, Pair<ValueSet<V>> b) {
        VertexClass<V> result = merge(a, b, (x, y) -> x.union(y));
        return result;
    }

    public static <V> VertexClass<V> merge(Pair<ValueSet<V>> a, Pair<ValueSet<V>> b, BinaryOperator<ValueSet<V>> op) {
        List<ValueSet<V>> tmp = Stream.of(0, 1).map(i -> {
            ValueSet<V> x = a.get(i);
            ValueSet<V> y = b.get(i);

            //ValueSet<V> r = x.union(y);
            ValueSet<V> r = op.apply(x, y);
            return r;
        }).collect(Collectors.toList());

        VertexClass<V> result = VertexClass.create(tmp);
        return result;
    }




    public static <V> VertexClass<V> create(List<ValueSet<V>> nodes) {
        VertexClass<V> result = new VertexClass<>(nodes.get(0), nodes.get(1));
        return result;
    }

    public VertexClass() {
        this(ValueSet.createEmpty(), ValueSet.createEmpty());
    }

    public VertexClass(ValueSet<V> fwdNodes, ValueSet<V> bwdNodes) {
        super(fwdNodes, bwdNodes);
    }

    public ValueSet<V> getFwdNodes() {
        return key;
    }

    public ValueSet<V> getBwdNodes() {
        return value;
    }

    /**
     * Reverse the direction of a predicate class
     *
     * @param pc
     * @return
     */
    public static PredicateClass reverse(PredicateClass pc) {
        PredicateClass result = new PredicateClass(pc.getBwdNodes(), pc.getFwdNodes());
        return result;
    }

//    public boolean contains(P_Path0 path) {
//        Node node = path.getNode();
//        boolean result = path.isForward()
//            ? key.contains(node)
//            : value.contains(node)
//            ;
//       return result;
//    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((value == null) ? 0 : value.hashCode());
        result = prime * result
                + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PredicateClass other = (PredicateClass) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[key=" + key + ", value=" + value
                + "]";
    }

}