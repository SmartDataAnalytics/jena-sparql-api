package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Set view over the values of a property of a given subject resource.
 * 
 * @author raven Mar 30, 2018
 *
 * @param <T>
 */
public class SetFromResourceAndInverseProperty<T extends Resource>
	extends AbstractSet<T>
{
	protected RDFNode subject;
	protected Property invProperty;
	protected Class<T> clazz;

	public SetFromResourceAndInverseProperty(RDFNode subject, Property invProperty, Class<T> clazz) {
		super();
		this.subject = subject;
		this.invProperty = invProperty;
		this.clazz = clazz;
	}

	@Override
	public boolean add(T o) {
		boolean result = ResourceUtils.addReverseProperty(subject, invProperty, o);
		return result;
	}
		
	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = ResourceUtils.listReversePropertyValues(subject, invProperty, clazz);
		return result;
	}

	@Override
	public int size() {
		int result = Iterators.size(iterator());
		return result;
	}
}
