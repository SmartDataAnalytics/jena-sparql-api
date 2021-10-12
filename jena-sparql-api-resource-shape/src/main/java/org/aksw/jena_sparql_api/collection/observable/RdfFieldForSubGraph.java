package org.aksw.jena_sparql_api.collection.observable;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.aksw.jena_sparql_api.schema.PropertySchema;
import org.apache.jena.graph.Node;


/**
 * An observable field over the subgraph formed by the triples of another
 * graph matching the given DirectedFilteredTriplePattern.
 *
 * @author raven
 *
 */
public class RdfFieldForSubGraph
    implements RdfField
{
    protected GraphChange graph;
    protected DirectedFilteredTriplePattern dftp;

    protected boolean isDeleted = false;
    protected boolean isIntensional = false;



    public RdfFieldForSubGraph(GraphChange graph, DirectedFilteredTriplePattern dftp) {
        super();
        this.graph = graph;
        this.dftp = dftp;
    }

    @Override
    public PropertySchema getPropertySchema() {
        return null;
    }

    @Override
    public Node getSourceNode() {
        return dftp.getSource();
    }


    @Override
    public void setIntensional(boolean onOrOff) {
        isIntensional = onOrOff;
    }

    @Override
    public boolean isIntensional() {
        return isIntensional;
    }

    @Override
    public void setDeleted(boolean onOrOff) {
        isDeleted = onOrOff;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }


    /**
     * A view on the set of base values.
     * Removing a triple marks the triple as deleted.
     * If the field is set to deleted, then the returned set is empty
     *
     */
//    @Override
//    public ObservableCollection<Node> getBaseAsSet() {
//        ObservableGraph baseGraph = graph.getBaseGraph();
//        ObservableCollection<Node> result = SetOfNodesFromGraph.create(baseGraph, dftp);
//
//        return result;
//    }

    @Override
    public ObservableCollection<Node> getEffectiveAsSet() {
        throw new RuntimeException("not implemented");
//        ObservableGraph baseGraph = graph.getObservableDelta();
//        ObservableCollection<Node> result = SetOfNodesFromGraph.create(baseGraph, dftp);
//
//        return result;
    }

//    public ObservableCollection<Node> getDeletedAsSet() {
//        ObservableGraph baseGraph = graph.getBaseGraph();
//        ObservableCollection<Node> result = SetOfNodesFromGraph.create(baseGraph, dftp);
//
//        return result;
//    }


    /** A mutable set view of explicitly added new values.
     * The new triples are affected by node remapping but not by triple-remapping.
     *
     */
    @Override
    public ObservableCollection<Node> getAddedAsSet() {
        ObservableGraph g = graph.getAdditionGraph();
        ObservableCollection<Node> result = SetOfNodesFromGraph.create(g, dftp);

        return result;
    }


//    public ObservableCollection<Node> getEffectiveValuesAsSet() {
//        ObservableCollection<Node> a = getBaseAsSet();
//        ObservableCollection<Node> b = getAddedAsSet();
//
//        ObservableSet<Node> result = null; //ObservableSetUnion<
//        return result;
//    }

//    public ObservableValue<Node> getAsValue() {
//
//    }
}
