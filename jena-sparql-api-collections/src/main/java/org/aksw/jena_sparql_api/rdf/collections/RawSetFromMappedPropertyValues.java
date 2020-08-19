package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

public class RawSetFromMappedPropertyValues
    extends AbstractSet<RDFNode>
    implements RdfBackedCollection<RDFNode>
{
    protected Resource subject;
    protected Property property;
    protected boolean isFwd;
    protected NodeMapper<?> nodeMapper;

    public RawSetFromMappedPropertyValues(Resource subject, Property property, NodeMapper<?> nodeMapper) {
        this(subject, property, true, nodeMapper);
    }

    public RawSetFromMappedPropertyValues(Resource subject, Property property, boolean isFwd,
            NodeMapper<?> nodeMapper) {
        super();
        this.subject = subject;
        this.property = property;
        this.isFwd = isFwd;
        this.nodeMapper = nodeMapper;
    }

    @Override
    public boolean add(RDFNode rdfNode) {
        // Validate whether the RDF node can be mapped by the nodeMapper
        Node node = rdfNode.asNode();
        boolean isValid = nodeMapper.canMap(node);
        boolean result;
        if(isValid) {
            result = ResourceUtils.addProperty(subject, property, isFwd, rdfNode);
        } else {
            throw new IllegalArgumentException("Argument " + rdfNode + " not accepted by nodeMapper");
        }
        return result;
    }

    @Override
    public void clear() {
        ResourceUtils.setProperty(subject, property, isFwd, nodeMapper, null);
    }

    @Override
    public Iterator<RDFNode> iterator() {
        ExtendedIterator<RDFNode> result = ResourceUtils.listProperties(subject, property, isFwd, nodeMapper)
                .mapWith(stmt -> ResourceUtils.getTarget(stmt, isFwd));

        return result;
    }

    @Override
    public int size() {
        int result = Iterators.size(iterator());
        return result;
    }

    @Override
    public Set<RDFNode> getRawCollection() {
        return new RawSetFromMappedPropertyValues(subject, property, isFwd, nodeMapper);
    }
}
