package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

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
