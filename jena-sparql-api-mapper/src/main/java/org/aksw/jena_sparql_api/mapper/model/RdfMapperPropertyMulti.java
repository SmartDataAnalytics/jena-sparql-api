package org.aksw.jena_sparql_api.mapper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.mapper.impl.type.EntityFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.PathFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.PlaceholderInfo;
import org.aksw.jena_sparql_api.mapper.impl.type.ResolutionTask;
import org.aksw.jena_sparql_api.mapper.impl.type.ResolutionTaskBase;
import org.aksw.jena_sparql_api.mapper.impl.type.ResourceFragment;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;

public class RdfMapperPropertyMulti
extends RdfMapperPropertyBase
{
    public RdfMapperPropertyMulti(
            PropertyOps propertyOps,
            Property predicate,
            RdfType targetRdfType,
            BiFunction<Object, Object, Node> createTargetIri,
            TypeConverter typeConverter) { // String fetchMode) {
        super(propertyOps, predicate, targetRdfType, createTargetIri, typeConverter);
    }

//    @Override
//    public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {
//        // TODO Auto-generated method stub
//        super.exposeFragment(out, priorState, entity);
//    }
    //
    //@Override
    //public void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject, Graph shapeGraph, Consumer<Triple> outSink) {
    //    Object value = propertyOps.getValue(entity);
    //
    //    if(value != null) {
    //        Supplier<Node> defaultNodeGenerator = createTargetNode == null
    //                ? null
    //                : () -> createTargetNode.apply(entity, value);
    //
    //        Node o = emitterContext.requestResolution(value);//, targetRdfType, defaultNodeGenerator);
    //        Triple t = new Triple(subject, predicate, o);
    //        outSink.accept(t);
    //
    //        // maybe we should write triples to the emitter context, as references
    //        // need to be resolved anyway
    //        //emitterContext.accept(t);
    //    }
    //}

    @Override
    public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {

        Resource s = out.getResource();
        Model tmp = s.getModel();

        Collection vs = (Collection)propertyOps.getValue(entity);

        for(Object v : vs) {
            Resource o = tmp.createResource();
            s.addProperty(predicate, o);

            PlaceholderInfo info = new PlaceholderInfo(null, targetRdfType, entity, null, propertyOps, v, null, this);



//        Map<RDFNode, Object> placeholders = new HashMap<>();
//        placeholders.put(o, v);

            out.getPlaceholders().put(o, info);
        }
    }

    @Override
    public void populate(EntityFragment out, Resource shape, Object entity) {
        List<Statement> stmts = shape.listProperties(predicate).toList();

        List<PlaceholderInfo> pis = new ArrayList<>();

        for(Statement stmt : stmts) {
            RDFNode o = stmt == null ? null : stmt.getObject();

            pis.add(new PlaceholderInfo(null, targetRdfType, entity, null, propertyOps, null, o, this));
        }

        //out.getPropertyInfos().put(key, value);
        out.getTasks().add(new ResolutionTaskBase<PlaceholderInfo>(pis) {
            @Override
            public Collection<ResolutionTask<PlaceholderInfo>> resolve(List<Object> resolutions) {
                //Object value = resolutions.get(0);
                // TODO The collection type has to be injected from elsewhere
                Object value = new HashSet<>(resolutions);

                propertyOps.setValue(entity, value);
                return Collections.emptyList();
            }
        });
    }
    //@Override
    //public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Graph inGraph, Node subject, Consumer<Triple> outSink) {
    //    List<Triple> triples = inGraph.find(subject, predicate, Node.ANY).toList();
    //
    //    Triple t = Iterables.getFirst(triples, null);
    //
    //    Node node;
    //    if(t != null) {
    //        node = t.getObject();
    //        outSink.accept(t);
    //    } else {
    //        node = null;
    //    }
    //
    //    if(node == null) {
    //    	if(createTargetNode != null) {
    //        	Object childEntity = propertyOps.getValue(entity);
    //        	if(childEntity != null) {
    //        		node = createTargetNode.apply(entity, childEntity);
    //        	}
    //    	}
    //    }
    //
    //    if(node != null) {
    //    	persistenceContext.requestResolution(propertyOps, entity, node);
    //    }
    //
    //    //persistenceContext.requestResolution(entity, propertyOps, subject, rdfType);
    //
    //
    ////    Object value = node == null
    ////            ? null
    ////            : persistenceContext.entityFor(new TypedNode(targetRdfType, node))
    ////            ;//rdfType.createJavaObject(node);
    ////
    ////
    ////    // We cannot set property values of primitive types to null
    ////    Class<?> valueType = propertyOps.getType();
    ////    if(value == null && valueType.isPrimitive()) {
    ////        value = Defaults.defaultValue(valueType);
    ////    }
    ////    propertyOps.setValue(entity, value);
    //}
    //
    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        shapeBuilder.out(predicate);
    //	ResourceShapeBuilder targetShape = shapeBuilder.outgoing(predicate);

    //	if("eager".equals(fetchMode)) {
    //		targetRdfType.build(targetShape);
    //	}
    }

    @Override
    public String toString() {
        return "RdfPopulatorPropertySingle [propertyName=" + propertyOps.getName()
                + ", predicate=" + predicate + ", targetRdfType="
                + targetRdfType + "]";
    }

    @Override
    public PropertyOps getPropertyOps() {
        return propertyOps;
    }

    @Override
    public PathFragment resolve(String propertyName) {
        PathFragment result = null;

        boolean isMatchingProperty = propertyOps.getName().equals(propertyName);

        if(isMatchingProperty) {
            Relation relation;
            if(typeConverter == null) {
                relation = RelationUtils.createRelation(predicate.asNode(), false);
            } else {
                ElementGroup group = new ElementGroup();
                group.addElement(ElementUtils.createElement(new Triple(Vars.s, predicate.asNode(), Vars.x)));
                Expr expr = typeConverter.toJava(new ExprVar(Vars.x));
                group.addElement(new ElementBind(Vars.o, expr));
                relation = new Relation(group, Vars.s, Vars.o);
            }


            result = new PathFragment(relation, propertyOps.getType(), targetRdfType, null);
        }


        return result;
    }

    //@Override
    //public Object readPropertyValue(Graph graph, Node subject) {
    //	// TODO Auto-generated method stub
    //	return null;
    //}


}
