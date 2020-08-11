package org.aksw.jena_sparql_api.sparql_path2;

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
    extends VertexClass<Node>
{
    private static final long serialVersionUID = -393920412450119L;

    public PredicateClass(ValueSet<Node> fwdNodes, ValueSet<Node> bwdNodes) {
        super(fwdNodes, bwdNodes);
    }

    
    public static boolean matchesStep(PredicateClass pclass, P_Path0 step) {
    	boolean result = step.isForward()
    			? pclass.getFwdNodes().contains(step.getNode())
    			: pclass.getBwdNodes().contains(step.getNode());

    	return result;
    }
    
    @Override
    public String toString() {
        return "PredicateClass [key=" + key + ", value=" + value
                + "]";
    }
}