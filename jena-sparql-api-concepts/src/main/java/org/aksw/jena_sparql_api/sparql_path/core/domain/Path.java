package org.aksw.jena_sparql_api.sparql_path.core.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;


public class Path {
	private List<Step> steps;
	
	public Path() {
		this(new ArrayList<Step>());
	}
	
	public Path(List<Step> steps) {
		this.steps = steps;
	}

	public List<Step> getSteps() {
		return steps;
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
		Path other = (Path) obj;
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
	
	
	public static List<Element> pathToElements(Path path, Var start, Var end, Generator generator) {
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

	
	public static List<Triple> pathToTriples(Path path, Var start, Var end, Generator generator) {
		List<Triple> result = new ArrayList<Triple>();
		
		Var a = start;
		
		Iterator<Step> it = path.getSteps().iterator();
		while(it.hasNext()) {
			Step step = it.next();
			
			Var b;
			if(it.hasNext()) {
				b = Var.alloc(generator.next());
			} else {
				b = end;
			}
			
			Triple t;
			if(!step.isInverse()) {
				t = new Triple(a, Node.createURI(step.getPropertyName()), b);
			}
			else {
				t = new Triple(b, Node.createURI(step.getPropertyName()), a);
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
}