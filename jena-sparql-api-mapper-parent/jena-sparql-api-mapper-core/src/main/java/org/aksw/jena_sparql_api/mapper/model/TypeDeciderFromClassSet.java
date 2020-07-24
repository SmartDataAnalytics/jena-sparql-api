package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.Multimap;

public class TypeDeciderFromClassSet
    implements TypeDecider
{
    protected Multimap<Class<?>, BiPredicate<? super RDFNode, ? super Class<?>>> candidates;

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb, Class<?> clazz) {
    }

    @Override
    public Collection<Class<?>> getApplicableTypes(Resource subject) {
        Set<Class<?>> result = new HashSet<>();
        for(Entry<Class<?>, Collection<BiPredicate<? super RDFNode, ? super Class<?>>>> e : candidates.asMap().entrySet()) {
            Class<?> clazz = e.getKey();
            Collection<BiPredicate<? super RDFNode, ? super Class<?>>> predicates = e.getValue();

            boolean isAccepted = true;
            for(BiPredicate<? super RDFNode, ? super Class<?>> predicate : predicates) {
                isAccepted = isAccepted && predicate.test(subject, clazz);
                if(!isAccepted) {
                    break;
                }
            }

            if(isAccepted) {
                result.add(clazz);
            }
        }

        return result;
    }

    @Override
    public void writeTypeTriples(Resource outResource, Class<?> clazz) {
    }
}
