package org.aksw.jena_sparql_api.utils.model;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.stream.Stream;

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
public class SetFromLiteralPropertyValues<T>
	extends AbstractSet<T>
{
	protected Resource subject;
	protected Property property;
	protected Class<T> clazz;

	public SetFromLiteralPropertyValues(Resource subject, Property property, Class<T> clazz) {
		super();
		this.subject = subject;
		this.property = property;
		this.clazz = clazz;
	}

	@Override
	public boolean add(T o) {
		boolean result = ResourceUtils.addLiteral(subject, property, o);
		return result;
	}

	@Override
	public void clear() {
		ResourceUtils.updateLiteralProperty(subject, property, clazz, null);
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = stream().iterator();
		return result;
	}

	@Override
	public Stream<T> stream() {
		Stream<T> result = ResourceUtils.listLiteralPropertyValues(subject, property, clazz);
		return result;
	}

	@Override
	public int size() {
		int result = (int)stream().count();
		return result;
	}
}
