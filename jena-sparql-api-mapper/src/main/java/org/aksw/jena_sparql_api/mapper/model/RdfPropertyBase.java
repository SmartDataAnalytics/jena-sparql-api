package org.aksw.jena_sparql_api.mapper.model;

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeanWrapper;

public abstract class RdfPropertyBase
    implements RdfProperty
{
    protected BeanWrapper beanWrapper;
    protected PropertyDescriptor propertyDescriptor;
    protected RdfClass targetRdfClass;

    public RdfPropertyBase(BeanWrapper beanWrapper, PropertyDescriptor propertyDescriptor, RdfClass targetRdfClass) {
        super();
        this.beanWrapper = beanWrapper;
        this.propertyDescriptor = propertyDescriptor;
        this.targetRdfClass = targetRdfClass;
    }

    @Override
    public String getName() {
        String result = propertyDescriptor.getName();
        return result;
    }



//    @Override
//    public RdfClass getTargetRdfClass() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Relation getRelation() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public void writePropertyValue(Object obj, Graph outputGraph) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public Object readPropertyValue(Graph graph, Node subject) {
//        // TODO Auto-generated method stub
//        return null;
//    }

}
