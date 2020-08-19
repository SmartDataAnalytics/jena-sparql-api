package org.aksw.jena_sparql_api.mapper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.rdf.model.Resource;

public class TypeDeciderCompound
    implements TypeDecider
{
    protected List<TypeDecider> delegates;

    public TypeDeciderCompound() {
        this(new ArrayList<>());
    }

    public TypeDeciderCompound(List<TypeDecider> delegates) {
        super();
        this.delegates = delegates;
    }

    public List<TypeDecider> getDelegates() {
        return delegates;
    }


    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        for(TypeDecider delegate : delegates) {
            delegate.exposeShape(rsb);
        }
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb, Class<?> clazz) {
        for(TypeDecider delegate : delegates) {
            delegate.exposeShape(rsb, clazz);
        }
    }

    @Override
    public Collection<Class<?>> getApplicableTypes(Resource subject) {
        Collection<Class<?>> result = null;
        for(TypeDecider delegate : delegates) {
            result = delegate.getApplicableTypes(subject);
            if(result != null) {
                break;
            }
        }

        result = result == null ? Collections.emptySet() : null;

        return result;
    }

    @Override
    public void writeTypeTriples(Resource outResource, Class<?> clazz) {
        for(TypeDecider delegate : delegates) {
            delegate.writeTypeTriples(outResource, clazz);
        }
    }

}
