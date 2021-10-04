package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceState;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.path.Path;

public class ShapedNode {
    protected Node src;
    protected Resource res;

    protected ResourceCache resourceCache;
    protected Collection<NodeSchema> nodeSchemas;
    protected SparqlQueryConnection conn;



    public ShapedNode(Node src, Collection<NodeSchema> nodeSchemas, ResourceCache resourceCache,
            SparqlQueryConnection conn) {
        super();
        this.src = src;
        this.resourceCache = resourceCache;
        this.nodeSchemas = nodeSchemas;
        this.conn = conn;
    }


    public static ShapedNode create(Node src, NodeSchema nodeSchema, ResourceCache resourceCache, SparqlQueryConnection conn) {
        return create(src, Collections.singleton(nodeSchema), resourceCache, conn);
    }

    public static ShapedNode create(Node src, Collection<NodeSchema> nodeSchemas, ResourceCache resourceCache, SparqlQueryConnection conn) {
        return new ShapedNode(src, nodeSchemas, resourceCache, conn);
    }

    public ResourceCache getResourceCache() {
        return resourceCache;
    }

    public ResourceState getResourceState() {
        return resourceCache.get(src);
    }


    public Node getSourceNode() {
        return src;
    }

    public SparqlQueryConnection getConnection() {
        return conn;
    }

    /** Return all paths demanded by the schemas */
//    protected Set<Path> getPathsFromSchema() {
//        for (NodeSchema ns : nodeSchemas) {
//            for (PropertySchema ps : nodeSchema.getPredicateSchemas()) {
//                Path path = null;
//            }
//        }
//
//
//        return null;
//    }

//    protected boolean canServeFromCache(Path path) {
//        state.get
//
//        return false;
//    }



    public Map<Path, ShapedProperty> getShapedProperties() {
        Map<Path, ShapedProperty> result = new LinkedHashMap<>();

        for (NodeSchema nss : nodeSchemas) {
            for (PropertySchema pss : nss.getPredicateSchemas()) {
                Path path = pss.getPath();

                ShapedProperty sp = result.computeIfAbsent(path, p -> new ShapedProperty(
                        this, path));
                sp.addShape(pss);
            }
        }

        return result;
    }


    @Override
    public String toString() {
        return "ShapedNode [" + src + "]";
    }

    public ShapedProperty getShapedProperty(Path path) {
        Map<Path, ShapedProperty> map = getShapedProperties();
        ShapedProperty result = map.get(path);
        return result;
    }
}
