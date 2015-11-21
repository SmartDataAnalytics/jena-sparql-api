package org.aksw.jena_sparql_api.mapper.trash;

import java.beans.PropertyDescriptor;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.mapper.model.RdfPropertyBase;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public class RdfPropertyDatatypeOld
    extends RdfPropertyBase
{
    protected Node predicate;
    protected RdfValueMapper rdfValueMapper;

    public RdfPropertyDatatypeOld(BeanWrapper beanWrapper, PropertyDescriptor propertyDescriptor,
            RdfClass targetRdfClass, Node predicate, RdfValueMapper rdfValueMapper) {
        super(beanWrapper, propertyDescriptor, targetRdfClass);
        this.predicate = predicate;
        this.rdfValueMapper = rdfValueMapper;
    }

    @Override
    public RdfClass getTargetRdfType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Relation getRelation() {
        Relation result = RelationUtils.createRelation(predicate.getURI(), false, null);
        return result;
    }

    @Override
    public void writePropertyValue(Graph outputGraph, Object obj, Node subject) {
        String propertyName = propertyDescriptor.getName();

        BeanWrapper bean = new BeanWrapperImpl(obj);
        Object value = bean.getPropertyValue(propertyName);

        //Object value = beanWrapper.getPropertyValue(propertyName);
        rdfValueMapper.writeValue(value, subject, predicate, outputGraph);
    }

    @Override
    public Object readPropertyValue(Graph graph, Node subject) {
        Object result = rdfValueMapper.readValue(graph, subject, predicate);
        return result;
    }

}
