package org.aksw.jena_sparql_api.mapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.aksw.jena_sparql_api.concepts.PropertyRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;
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
    private static final Logger logger = LoggerFactory.getLogger(RdfPropertySeq.class);

    //protected EntityManagerRdf;
    //protected

    protected Quad quad; // g, s, p; - object position should be Node.ANY - is ignored

    protected Function<Object, Node> objectToNode;

    protected RdfClass targetRdfClass;

    public static final PropertyRelation seqRelation = PropertyRelation.create("?s ?p ?o . Filter(regex(?p, 'http://www.w3.org/1999/02/22-rdf-syntax-ns#_[0-9]+'))", "s", "p", "o");
    public static final Expr seqExpr = ExprUtils.parse("regex(?p, 'http://www.w3.org/1999/02/22-rdf-syntax-ns#_[0-9]+')");


    public void toRdf(DatasetGraph target, List<Object> items) {
    }


    public List<Object> toJava(DatasetGraph datasetGraph) {
        return null;
    }

}
