package org.aksw.jena_sparql_api.rdf.collections;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

public interface RdfBackedCollection<T>
    extends Collection<T>
{
    /**
     * A collection of the raw backing RDFNode objects.
     * For example, a Collection<String> may be backed by an RDF resource with a property
     * that leads to a collection of IRIs or literals of type xsd:string.
     *
     * @return
     */
    Collection<RDFNode> getRawCollection();
}
