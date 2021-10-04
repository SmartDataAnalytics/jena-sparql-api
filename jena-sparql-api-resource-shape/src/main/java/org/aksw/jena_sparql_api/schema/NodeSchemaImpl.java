package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.apache.jena.graph.Node;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class NodeSchemaImpl
    implements NodeSchema
{
    /** Mapping of specific predicates */
    protected Table<Node, Boolean, PropertySchema> predicateViews = HashBasedTable.create();

    /** Generic triple triple patterns where the predicate may be a variable */
    protected Set<DirectedFilteredTriplePattern> triplePatterns = new LinkedHashSet<>();


    public PropertySchema createPropertySchema(Node predicate, boolean isForward) {
        PropertySchema spec = new PropertySchemaImpl(predicate, isForward);
        predicateViews.put(predicate, isForward, spec);

        return spec;
    }

    public Set<DirectedFilteredTriplePattern> getGenericPatterns() {
        return triplePatterns;
    }


    public Collection<PropertySchema> getPredicateSchemas() {
        return predicateViews.values();
    }

//    public NodeGraphView instantiate(Node node) {
//        // return new NodeGraphView(graph, source, this);
//        return null;
//    }

//    public long copyMatchingTriples(Node source, Graph targetGraph, Graph sourceGraph) {
//        long result = 0;
//
//        for (PropertySchema predicateSchema : getPredicateSchemas()) {
//            long contrib = predicateSchema.copyMatchingTriples(source, targetGraph, sourceGraph);
//            result += contrib;
//        }
//
//        return result;
//    }

}