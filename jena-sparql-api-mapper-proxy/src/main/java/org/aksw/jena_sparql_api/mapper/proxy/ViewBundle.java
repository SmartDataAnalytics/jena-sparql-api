package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

/**
 * A ViewBundle holds two collection views over an RDF graph:
 * The rawView is a collection of raw {@link RDFNode}s (having the same Model), whereas the javaView is
 * derived from the rawView using some conversion function over the items of the raw view.
 *
 * The view bundle main use case is to allow the skolemization system to operate on the
 * nodes in the RDF graph themselves rather then the java objects derived from them.
 *
 *
 * @author raven
 *
 */
public class ViewBundle {
    /**
     * The raw yield yields the set of RDFNodes that back the java view
     * (predicate, isFwd) -> (subject -> collectionOfRdfNodes)
     *
     */
    protected Collection<RDFNode> rawView;

    /**
     * The function that yields the appropriate java type
     * (predicate, isFwd) -> (subject -> javaObject)
     *
     * @return
     */
    protected Object javaView;


    public ViewBundle(Collection<RDFNode> rawView, Object javaView) {
        super();
        this.rawView = rawView;
        this.javaView = javaView;
    }


    public Collection<RDFNode> getRawView() {
        return rawView;
    }


    public Object getJavaView() {
        return javaView;
    }
}
