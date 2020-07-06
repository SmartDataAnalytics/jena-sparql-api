package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;



public class SetFromMappedPropertyValues<T>
    extends AbstractSet<T>
{
    protected Resource subject;
    protected Property property;
    protected boolean isFwd;
    protected NodeMapper<T> nodeMapper;

    public SetFromMappedPropertyValues(Resource subject, Property property, NodeMapper<T> nodeMapper) {
        this(subject, property, true, nodeMapper);
    }

    public SetFromMappedPropertyValues(Resource subject, Property property, boolean isFwd, NodeMapper<T> nodeMapper) {
        super();
        this.subject = subject;
        this.property = property;
        this.isFwd = isFwd;
        this.nodeMapper = nodeMapper;
    }

    @Override
    public boolean add(T obj) {
        Node node = nodeMapper.toNode(obj);
        RDFNode rdfNode = subject.getModel().asRDFNode(node);
        boolean result = ResourceUtils.addProperty(subject, property, isFwd, rdfNode);
        return result;
    }

    @Override
    public void clear() {
        ResourceUtils.setProperty(subject, property, isFwd, nodeMapper, null);
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = ResourceUtils.listPropertyValues(subject, property, isFwd, nodeMapper);
        return result;
    }

    @Override
    public int size() {
        int result = Iterators.size(iterator());
        return result;
    }
}
