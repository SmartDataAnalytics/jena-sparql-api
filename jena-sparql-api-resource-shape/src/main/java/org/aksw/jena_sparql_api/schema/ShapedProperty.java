package org.aksw.jena_sparql_api.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceState;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceFromList;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapServiceFromListService;
import org.aksw.jena_sparql_api.lookup.MapServiceSparqlQuery;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.Path;


public class ShapedProperty {
    protected ShapedNode src;
    protected Path path;

    /** The set of property schemas of src sharing the same path */
    protected Collection<PropertySchema> propertySchemas;

    public ShapedProperty(ShapedNode src, Path path) {
        this(path, new ArrayList<>());
        this.src = src;
    }

    public Path getPath() {
        return path;
    }

    public ShapedProperty(Path path, Collection<PropertySchema> propertySchemas) {
        super();
        this.path = path;
        this.propertySchemas = propertySchemas;
    }

    public void addShape(PropertySchema propertySchema) {
        propertySchemas.add(propertySchema);
    }


    protected ShapedNode createTargetShapedNode(Node tgt, Collection<NodeSchema> nodeSchemas) {
        return ShapedNode.create(tgt, nodeSchemas, src.getResourceCache(), src.getConnection());
    }

    protected Set<Node> getCachedValues() {
        ResourceCache resourceCache = src.getResourceCache();
        ResourceState rs = resourceCache.get(src.getSourceNode());
        Set<Node> cachedValues = rs == null ? null : rs.getFromCache(path);

        return cachedValues;
    }

    protected boolean isInMemory() {
        boolean result = getCachedValues() != null;
        return result;
    }

    public boolean isEmpty() {
        Set<Node> cachedValues = getCachedValues();
        boolean result = cachedValues != null
                ? cachedValues.isEmpty()
                : getValues().streamData(null, RangeUtils.rangeStartingWithZero).isEmpty().blockingGet();

        return result;
    }

    public MapService<Concept, Node, ShapedNode> getValues() {
        ResourceCache resourceCache = src.getResourceCache();
//        ResourceState rs = resourceCache.get(src.getSourceNode());
        Set<Node> cachedValues = getCachedValues();
        SparqlQueryConnection conn = src.getConnection();


        MapService<Concept, Node, Table> core;
        // core = ListServiceFromList.wrap(cachedValues, null);

        Set<NodeSchema> tgtNodeSchemas = getTargetNodeSchemas();

        if (cachedValues != null) {
            List<Node> items = new ArrayList<>(cachedValues);
            ListService<Concept, Node> listService = ListServiceFromList.wrap(items, (a, b) -> true);

            // MapService<Concept, Node, Table> x;


//            new MapServiceFromListService<>(null, null, null)
            // FIXME Filtering
            // MapService<Concept, Node, ShapedNode> listService =

            core = new MapServiceFromListService<>(listService, n -> n, n -> {
                Table table = null;
                // ShapedNode r = new ShapedNode(node, resourceCache, tgtNodeSchemas, conn);
                return table;
            });


        } else {
            TriplePath tp = new TriplePath(src.getSourceNode(), path, Vars.o);
            Query query = new Query();
            query.setQuerySelectType();
            query.getProject().add(Vars.o);
            query.setQueryPattern(ElementUtils.createElement(tp));

            core = new MapServiceSparqlQuery(src.getConnection(), query, Vars.o);
        }



        // result = new ListServiceSparqlQuery.


        // boolean result = set != null;
        MapService<Concept, Node, ShapedNode> result = core.transformValues((k, v) -> {
            ShapedNode r = ShapedNode.create(k, tgtNodeSchemas, resourceCache, conn);
            return r;
        });

        return result;
    }

    public Set<NodeSchema> getTargetNodeSchemas() {
        Set<NodeSchema> tgtNodeSchemas = propertySchemas.stream()
                .map(PropertySchema::getTargetSchemas)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        return tgtNodeSchemas;
    }

    /*
    boolean canServeFromCache() {
        ResourceState rs = src.getResourceState();


        Set<Node> set = rs.getFromCache(path);
        boolean result = set != null;
        return result;
    }
    */
}


