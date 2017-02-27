package org.aksw.jena_sparql_api.concepts;

import java.util.List;

import org.apache.jena.query.SortCondition;

/**
 * A SPARQL concept with a defined ordering
 * 
 * @author raven
 *
 */
public class OrderedConcept {
	protected Concept concept;
	protected List<SortCondition> orderBy;
	
	public OrderedConcept(Concept concept, List<SortCondition> orderBy) {
		super();
		this.concept = concept;
		this.orderBy = orderBy;
	}

	public Concept getConcept() {
		return concept;
	}

	public List<SortCondition> getOrderBy() {
		return orderBy;
	}

	@Override
	public String toString() {
		return "OrderedConcept [concept=" + concept + ", orderBy=" + orderBy + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
		result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
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
		OrderedConcept other = (OrderedConcept) obj;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		if (orderBy == null) {
			if (other.orderBy != null)
				return false;
		} else if (!orderBy.equals(other.orderBy))
			return false;
		return true;
	}
	
}
