package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
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
    implements RdfBackedCollection<T>
{
    protected Resource subject;
    protected Property property;
    protected boolean isFwd;
    protected Class<T> clazz;

    public SetFromPropertyValues(Resource subject, Property property, Class<T> clazz) {
        this(subject, property, true, clazz);
    }

    public SetFromPropertyValues(Resource subject, Property property, boolean isFwd, Class<T> clazz) {
        super();
        this.subject = subject;
        this.property = property;
        this.isFwd = isFwd;
        this.clazz = clazz;
    }

    @Override
    public boolean add(T o) {
        boolean result = ResourceUtils.addProperty(subject, property, isFwd, o);
        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = false;
        if(o instanceof RDFNode) {
            RDFNode n = (RDFNode)o;
            Model m = subject.getModel();
            result = isFwd
                ? m.contains(subject, property, n)
                : n.isResource() ? m.contains(n.asResource(), property, subject) : false;
        }

        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = ResourceUtils.listPropertyValues(subject, property, isFwd, clazz);
        return result;
    }


    @Override
    public int size() {
        int result = Iterators.size(iterator());
        return result;
    }

    @Override
    public Collection<RDFNode> getRawCollection() {
        return new SetFromPropertyValues<>(subject, property, isFwd, RDFNode.class);
    }
}