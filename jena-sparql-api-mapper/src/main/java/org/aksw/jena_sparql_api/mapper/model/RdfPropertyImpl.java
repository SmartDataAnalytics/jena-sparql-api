package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

public class RdfPropertyImpl
    implements RdfProperty
{
    //protected RdfClass rdfClass;
    //protected  collectiontype

    //protected PropertyDescriptor propertyDescription;

    /**
     * The (java) name of the attribute
     */
    protected String propertyName;

    //protected BeanWrapper owningBean;

    /**
     * The corresponding RDF predicate
     */
    protected Relation relation;

    protected RdfType targetRdfType;

    /**
     * The type can be either simply a class (including primitive ones), but it can also be
     * a parameterized class, such as a List&lt;Person&gt;
     *
     */
    protected RdfType rdfType;


    public RdfPropertyImpl(String name, Relation relation, RdfType targetRdfType) {
        super();
        this.propertyName = name;
        this.relation = relation;
        this.targetRdfType = targetRdfType;
    }

    public String getName() {
        return propertyName;
    }

    public Relation getRelation() {
        return relation;
    }

    public RdfType getTargetRdfType() {
        return targetRdfType;
    }


    @Override
    public void writePropertyValue(Graph out, Object obj, Node subject) {
        Node o = targetRdfType.getRootNode(obj);

        Triple tmp = RelationUtils.extractTriple(relation);
        Node p = tmp.getPredicate();

        //Quad t = new Quad(Quad.defaultGraphIRI, subject, p, o);
        Triple t = new Triple(subject, p, o);

        if(!out.contains(t)) {
            out.add(t);

            targetRdfType.writeGraph(out, obj);
        }
    }

    @Override
    public Object readPropertyValue(Graph graph, Node subject) {



        // TODO Auto-generated method stub
        return null;
    }
}
