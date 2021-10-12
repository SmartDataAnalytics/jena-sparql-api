package org.aksw.jena_sparql_api.collection.observable;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;


/**
 * An RdfField is a reference to a set of triples based on a source node and a PropertySchema..
 *
 * @author raven
 *
 */
public interface RdfField {
    /**
     * Returns the schema of the field. The schema allows testing for whether a specific
     * triple matches it.
     */
    PropertySchema getPropertySchema();

    /**
     * A field is based on a specific node in the RDF graph.
     * Depending on the property schema the values are obtained from ingoing and/or outgoing triples (or quads).
     *
     * @return
     */
    Node getSourceNode();

    /**
     * If false then the field only matches a specific set of triples based on a given graph.
     * If true then the field matches any triple that fits into the schema independent of any graph.
     *
     * Marking a field with intensional=true as deleted will delete triples by a pattern
     * such as DELETE WHERE { :foo :bar ?x }
     * whereas if intensional was false than deletion would explicitly enumerate triples - i.e.
     * DELETE DATA { :foo :bar v1, v2, v3 }
     *
     * @return
     */
    boolean isIntensional();
    void setIntensional(boolean onOrOff);

    void setDeleted(boolean onOrOff);
    boolean isDeleted();

//    ObservableCollection<Node> getBaseAsSet();
    ObservableCollection<Node> getAddedAsSet();

    ObservableCollection<Node> getEffectiveAsSet();




    default boolean matchesTriple(Triple t) {
        // TODO Maybe we should introduce a super class specifically for triple-centric schemas?
        // The point is that more complex schemas could match a set of triples comprising an rdf:List;
        //   in that case matching triples in isolation is meaningless
        PropertySchema schema = getPropertySchema();
        if (!schema.canMatchTriples()) {
            throw new RuntimeException("Schema does not support matching triples");
        }

        Node sourceNode = getSourceNode();

        boolean result = schema.matchesTriple(sourceNode, t);
        return result;
    }
}
