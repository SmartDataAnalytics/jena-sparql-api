package org.aksw.jena_sparql_api.utils.model;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.Iterators;

/**
 * Set view over the values of a property of a given subject resource.
 * 
 * @author raven Mar 30, 2018
 *
 * @param <T>
 */
public class SetFromPropertyValues<T extends RDFNode>
	extends AbstractSet<T>
{
	protected Resource subject;
	protected Property property;
	protected Class<T> clazz;

	public SetFromPropertyValues(Resource subject, Property property, Class<T> clazz) {
		super();
		this.subject = subject;
		this.property = property;
		this.clazz = clazz;
	}

	@Override
	public boolean add(T o) {
		boolean result = ResourceUtils.addProperty(subject, property, o);
		return result;
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = ResourceUtils.listPropertyValues(subject, property, clazz);
		return result;
	}


	@Override
	public int size() {
		int result = Iterators.size(iterator());
		return result;
	}}
