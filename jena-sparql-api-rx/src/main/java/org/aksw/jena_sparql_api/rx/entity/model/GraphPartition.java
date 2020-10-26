package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;

/**
 * A combination of a where pattern with designated variables that act as keys
 * and a graph pattern
 *
 * ENTITY ?v1 ... ?vn
 * CONSTRUCT { ?v1 rdfs:label ?}
 * WHERE { ?v1 ... ?vn }
 *
 * @author raven
 *
 */
public interface GraphPartition
{
    /**
     * Graph partitions in with a non-null fetch group name
     * will be fetched using separate lookups rather than combining their graph patterns
     * into the attribute part of the base query.
     *
     * Graph partitions in the same fetch group will be retrieved using a union.
     *
     * An example using virtuoso pragma style syntax would be:
     *
     * DEFINE :fetchGroup "foo" CONSTRUCT WHERE { ?x a ?t } PARTITION BY ?x
     * DEFINE :fetchGroup "foo" CONSTRUCT WHERE { ?y :label ?l} PARTITION BY ?y
     *
     * effective pattern (with ?z the aligned name to create a join on ?x and ?y):
     * { ?z a ?t . ?z label ?l }
     *
     */
    String getLazyFetchGroupName();
    void setLazyFetchGroupName(String name);


    EntityTemplate getEntityTemplate();

    Element getElement();
    void setElement(Element element);

    List<Var> getPartitionVars();

    GraphPartition applyNodeTransform(NodeTransform nodeTransform);
}

