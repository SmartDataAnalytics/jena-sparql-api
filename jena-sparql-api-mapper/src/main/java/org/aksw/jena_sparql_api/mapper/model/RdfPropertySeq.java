package org.aksw.jena_sparql_api.mapper.model;

import java.util.List;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A property that is serialized in RDF as a Seq
 *
 * class {
 *   @Iri("geom:geometry")
 *   @RdfSeq("") // by default, lists imply an RdfSeq, with a sub-iri generated as #{subject + '-' +  property.name}
 *   @DefaultIri("") // Rule for creating the collection resource
 *   List<Object> items;
 * }
 *
 *
 * @author raven
 *
 */
public class RdfPropertySeq {
    //protected EntityManagerRdf;
    //protected

    protected Quad quad; // g, s, p; - object position should be Node.ANY - is ignored

    protected Function<Object, Node> objectToNode;

    protected RdfClassImpl targetRdfClass;


    public void toRdf(DatasetGraph target, List<Object> items) {

        // Create the sub-property

        // Create memberships
        for(int i = 0; i < items.size(); ++i) {
            Object item = items.get(i);

            Node p = RDF.li(i + 1).asNode();

            Node node = objectToNode.apply(item);
            Quad quad = new Quad(quad.getGraph(), quad.getSubject(), quad.getPredicate(), node);

            target.add(quad);
        }

    }


    public List<Object> toJava(DatasetGraph datasetGraph) {
        return null;
    }

}
