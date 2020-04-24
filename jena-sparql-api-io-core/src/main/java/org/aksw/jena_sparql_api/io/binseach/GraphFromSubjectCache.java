package org.aksw.jena_sparql_api.io.binseach;

import org.apache.jena.ext.com.google.common.cache.Cache;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;

public class GraphFromSubjectCache
    extends GraphBase
{
    protected GraphBase delegate;
    protected Cache<Node, Graph> subjectCache;

    public GraphFromSubjectCache(GraphBase delegate, Cache<Node, Graph> subjectCache) {
        super();
        this.delegate = delegate;
        this.subjectCache = subjectCache;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        // For any triple pattern with a concrete subject, load all triples from the underlying graph
//        if(triplePattern.getSubject().) {
//
//        }

        return null;
    }

}
