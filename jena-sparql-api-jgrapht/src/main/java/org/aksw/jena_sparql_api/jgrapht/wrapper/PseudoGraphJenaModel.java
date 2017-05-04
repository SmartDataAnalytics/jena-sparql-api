package org.aksw.jena_sparql_api.jgrapht.wrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;


/**
 * Wrapper for exposing a Jena model as a JGraphT directed pseudo model.
 *
 *
 *
 * @author raven
 *
 */

public class PseudoGraphJenaModel
    implements DirectedGraph<RDFNode, Statement>
{
    protected Model model;
    protected Property predicate; // May be RDFNode.ANY

    protected transient EdgeFactory<RDFNode, Statement> edgeFactory;

    public PseudoGraphJenaModel(Model model, Property predicate) {
        super();
        this.model = model;
        this.predicate = predicate;

        edgeFactory = new EdgeFactoryJenaModel(model, predicate);
    }


    @Override
    public Set<Statement> getAllEdges(RDFNode sourceVertex, RDFNode targetVertex) {
        Set<Statement> result = sourceVertex.isResource()
                ? model.listStatements(sourceVertex.asResource(), predicate, targetVertex).toSet()
                : Collections.emptySet()
                ;

        return result;
        //return model.find(sourceVertex, predicate, targetVertex).toSet();
    }

    @Override
    public Statement getEdge(RDFNode sourceVertex, RDFNode targetVertex) {
        Set<Statement> edges = getAllEdges(sourceVertex, targetVertex);
        // TODO Maybe throw an exception or return null if there are multiple edges
        Statement result = edges.iterator().next();
        return result;
    }

    @Override
    public EdgeFactory<RDFNode, Statement> getEdgeFactory() {
        return edgeFactory;
    }

    @Override
    public Statement addEdge(RDFNode sourceVertex, RDFNode targetVertex) {
        Statement result;
        if(predicate == null || predicate.asNode().equals(Node.ANY)) {
            throw new UnsupportedOperationException("Cannot insert edge if the predicate is RDFNode.ANY");
        } else {
            result = model.createStatement(sourceVertex.asResource(), predicate, targetVertex);
            model.add(result);
        }
        return result;
    }

    @Override
    public boolean addEdge(RDFNode sourceVertex, RDFNode targetVertex, Statement e) {
        boolean result = !model.contains(e);
        if(result) {
            model.add(e);
        }
        return true;
    }

    @Override
    public boolean addVertex(RDFNode v) {
        // Silently ignore calls to this
        return false;
    }

    @Override
    public boolean containsEdge(RDFNode sourceVertex, RDFNode targetVertex) {
        // TODO Not sure if contains works with RDFNode.ANY - may have to use !.find().toSet().isEmpyt()
        boolean result = sourceVertex != null && sourceVertex.isResource() && model.contains(sourceVertex.asResource(), predicate, targetVertex);
        return result;
    }

    @Override
    public boolean containsEdge(Statement e) {
        boolean result = model.contains(e);
        return result;
    }

    @Override
    public boolean containsVertex(RDFNode v) {
        boolean result =
                (v != null && v.isResource() && model.contains(v.asResource(), null, (RDFNode)null)) ||
                model.contains(null, null, v);
        return result;
    }

    @Override
    public Set<Statement> edgeSet() {
        Set<Statement> result = listStatements(model, null, predicate, null);
        return result;
    }

    @Override
    public Set<Statement> edgesOf(RDFNode vertex) {
        Set<Statement> result = new HashSet<>();
        listStatements(result, model, vertex, predicate, null);
        listStatements(result, model, vertex, predicate, vertex);


        return result;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Statement> edges) {
        for(Statement edge : edges) {
            model.remove(edge);
        }
        return true;
    }

    @Override
    public Set<Statement> removeAllEdges(RDFNode sourceVertex, RDFNode targetVertex) {
        model.remove(sourceVertex.asResource(), predicate, targetVertex);
        return null;
    }

    @Override
    public boolean removeAllVertices(Collection<? extends RDFNode> vertices) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Statement removeEdge(RDFNode sourceVertex, RDFNode targetVertex) {
        //Model m = model.createStatement(sourceVertex.asResource(), predicate, targetVertex);
        //Statement result = new Statement(sourceVertex.asResource(), predicate, targetVertex);
        model.remove(sourceVertex.asResource(), predicate, targetVertex);
//    	model.remove(result.get, p, o);
//        return true;
        return null;
    }

    @Override
    public boolean removeEdge(Statement e) {
        model.remove(e.getSubject(), e.getPredicate(), e.getObject());
        return true;
    }


    @Override
    public boolean removeVertex(RDFNode v) {
        if(v.isResource()) {
            model.remove(v.asResource(), predicate, (RDFNode)null);
        }

        model.remove(null, predicate, v);
        return true;
    }


    @Override
    public Set<RDFNode> vertexSet() {
        Set<RDFNode> result = new HashSet<>();
        model.listStatements(null, predicate, (RDFNode)null).forEachRemaining(stmt -> {
                result.add(stmt.getSubject());
                result.add(stmt.getObject());
        });
        return result;
    }

    @Override
    public RDFNode getEdgeSource(Statement e) {
        return e.getSubject();
    }

    @Override
    public RDFNode getEdgeTarget(Statement e) {
        return e.getObject();
    }

    @Override
    public double getEdgeWeight(Statement e) {
        return 1;
    }

    @Override
    public int inDegreeOf(RDFNode vertex) {
        int result = incomingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Statement> incomingEdgesOf(RDFNode vertex) {
        Set<Statement> result = listStatements(model, null, predicate, vertex);
        return result;
    }

    @Override
    public int outDegreeOf(RDFNode vertex) {
        int result = outgoingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Statement> outgoingEdgesOf(RDFNode vertex) {
        Set<Statement> result = listStatements(model, vertex, predicate, null);
        return result;
    }

    public static Set<Statement> listStatements(Model model, RDFNode sourceVertex, RDFNode predicate, RDFNode targetVertex) {
        Set<Statement> result = new LinkedHashSet<>();
        listStatements(result, model, sourceVertex, predicate, targetVertex);
        return result;
    }

    public static void listStatements(Collection<Statement> result, Model model, RDFNode sourceVertex, RDFNode predicate, RDFNode targetVertex) {

        if(sourceVertex != null && !sourceVertex.isURIResource()) {
            result = Collections.emptySet();
        } else if(predicate != null && predicate.canAs(Property.class)) {
            result = Collections.emptySet();
        } else {
            Resource s = sourceVertex == null ? null : sourceVertex.asResource();
            Property p = predicate == null ? null : predicate.as(Property.class);

            result = model.listStatements(s, p, targetVertex).toSet();
        }

    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PseudoGraphJenaModel other = (PseudoGraphJenaModel) obj;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (predicate == null) {
            if (other.predicate != null)
                return false;
        } else if (!predicate.equals(other.predicate))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "PseudoGraphJena [model=" + model + ", predicate=" + predicate + "]";
    }
}
