package org.aksw.jena_sparql_api_sparql_path2;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

/**
 * The main reason this class extends pair is to have the list interface
 * which allows iterating the directions with get(0) and get(1)
 *
 * @author raven
 *
 */
public class PredicateClass
    extends Pair<ValueSet<Node>>
{
    private static final long serialVersionUID = -3939204124201128789L;

    public PredicateClass(ValueSet<Node> key, ValueSet<Node> value) {
        super(key, value);
    }

    public ValueSet<Node> getFwdNodes() {
        return key;
    }

    public ValueSet<Node> getBwdNodes() {
        return value;
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
        return "PredicateClass [key=" + key + ", value=" + value
                + "]";
    }

}