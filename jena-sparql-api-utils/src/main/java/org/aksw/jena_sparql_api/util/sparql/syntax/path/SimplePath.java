package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import com.google.common.base.Joiner;


public class SimplePath
	implements Comparable<SimplePath>
{
    private List<P_Path0> steps;

    public SimplePath() {
        this(new ArrayList<P_Path0>());
    }

    public SimplePath(List<P_Path0> steps) {
        this.steps = steps;
    }

    public List<P_Path0> getSteps() {
        return steps;
    }
    
//    public static org.apache.jena.sparql.path.Path toJena(Step step) {
//    	Node node = NodeFactory.createURI(step.getPropertyName());
//    	org.apache.jena.sparql.path.Path result = step.isInverse()
//    			? new P_ReverseLink(node)
//    			: new P_Link(node);
//    			
//    	return result;
//    }

    
    public static SimplePath fromPropertyPath(Path path) {
    	List<P_Path0> steps = PathUtils.toList(path);
    	return new SimplePath(steps);
    }
    
    public static Path toPropertyPath(SimplePath path) {
    	org.apache.jena.sparql.path.Path result = null;

    	List<P_Path0> steps = path.getSteps();
    	for(int i = 0; i < steps.size(); ++i) {
    		P_Path0 contrib = steps.get(i);
    		//org.apache.jena.sparql.path.Path contrib = step;//toJena(step);
    		
    		result = result == null ? contrib : new P_Seq(result, contrib);
    	}
    	
    	return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((steps == null) ? 0 : steps.hashCode());
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
        SimplePath other = (SimplePath) obj;
        if (steps == null) {
            if (other.steps != null)
                return false;
        } else if (!steps.equals(other.steps))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Path [steps=" + steps + "]";
    }


    public static List<Element> pathToElements(SimplePath path, Var start, Var end, Generator generator) {
        List<Element> result = new ArrayList<Element>();

        ElementTriplesBlock tmp = new ElementTriplesBlock();

        List<Triple> triples = pathToTriples(path, start, end, generator);

        if(!triples.isEmpty()) {
            for(Triple triple : triples) {
                tmp.addTriple(triple);
            }

            result.add(tmp);
        }

        return result;
    }


    public static List<Triple> pathToTriples(SimplePath path, Var start, Var end, Generator<Var> generator) {
        List<Triple> result = new ArrayList<Triple>();

        Var a = start;

        Iterator<P_Path0> it = path.getSteps().iterator();
        while(it.hasNext()) {
            P_Path0 step = it.next();

            Var b;
            if(it.hasNext()) {
                b = generator.next();//Var.alloc(generator.next());
            } else {
                b = end;
            }

            Triple t;
            if(step.isForward()) {
                t = new Triple(a, step.getNode(), b);
            }
            else {
                t = new Triple(b, step.getNode(), a);
            }

            result.add(t);

            a = b;
        }

        return result;
    }

    public String toPathString() {
        String result = Joiner.on(' ').join(steps);
        return result;

        /*
        String result = "";

        for(Step step : steps) {
            if(!result.isEmpty()) {
                result += " ";
            }

            if(step.isInverse()) {
                result += "<";
            }

            result += step.getPropertyName();
        }


        return result;
        */
    }

	@Override
	public int compareTo(SimplePath o) {
		int result = IterableUtils.compareByLengthThenItems(this.steps, o.steps, PathUtils::compareStep);
		return result;
	}
	
}