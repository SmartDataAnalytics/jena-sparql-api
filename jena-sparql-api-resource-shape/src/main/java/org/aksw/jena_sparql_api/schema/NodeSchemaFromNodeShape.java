package org.aksw.jena_sparql_api.schema;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.vocabulary.SH;

import com.google.common.base.Converter;
import com.google.common.collect.Iterables;

class ConverterFromRDFNodeView<A extends RDFNode, B extends RDFNode>
    extends Converter<A, B>
{
    protected Class<A> backendClass;
    protected Class<B> viewClass;

    public ConverterFromRDFNodeView(Class<A> backendClass, Class<B> viewClass) {
        super();
        this.backendClass = backendClass;
        this.viewClass = viewClass;
    }

    @Override
    protected B doForward(A a) {
        return a.as(viewClass);
    }

    @Override
    protected A doBackward(B b) {
        return b.as(backendClass);
    }

}

@ResourceView
public interface NodeSchemaFromNodeShape
    extends NodeSchema, Resource
{
    /** Return the underlying shacl shape */
    default SHNodeShape getNodeShape() {
        return as(SHNodeShape.class);
    }


    /** Scan all property schemas for one that matches the predicate and direction - runs in O(n) */
    default Stream<PropertySchemaFromPropertyShape> getPropertySchemas(Node predicate, boolean isForward) {
        return getPredicateSchemas().stream()
            .filter(item -> Objects.equals(item.getPredicate(), predicate) && item.isForward() == isForward);
    }

    @Override
    default PropertySchema createPropertySchema(Node predicate, boolean isForward) {
        Set<PropertySchemaFromPropertyShape> set = getPropertySchemas(predicate, isForward).collect(Collectors.toSet());
        PropertySchemaFromPropertyShape result = set.isEmpty() ? null : Iterables.getOnlyElement(set);

        if (result == null) {
            SHNodeShape nodeShape = getNodeShape();
            Model m = nodeShape.getModel();
            SHPropertyShape propertyShape = m.createResource().as(SHPropertyShape.class);

            Property p = new PropertyImpl(predicate, (ModelCom)m);

            Resource path = isForward
                    ? p
                    : m.createResource().addProperty(SH.inversePath, p);

            propertyShape.addProperty(SH.path, path);
            nodeShape.addProperty(SH.property, propertyShape);

            // result = new PropertySchemaFromPropertyShape(propertyShape);
        }
        return result;
    }

    @Override
    default Set<DirectedFilteredTriplePattern> getGenericPatterns() {
        return Collections.emptySet();
    }

    @Override
    default List<PropertySchemaFromPropertyShape> getPredicateSchemas() {
        SHNodeShape nodeShape = getNodeShape();
        List<PropertySchemaFromPropertyShape> result = nodeShape.getPropertyShapes().stream()
                .map(propertyShape -> propertyShape.as(PropertySchemaFromPropertyShape.class))
                .collect(Collectors.toList());

        return result;
//        Collection<SHPropertyShape> backend = getNodeShape().getPropertyShapes();
//        Collection<PropertySchema> result = ConvertingCollection.createSafe(backend, new ConverterFromRDFNodeView<>(SHPropertyShape.class, PropertySchema.class));
//        return result;
    }


}
