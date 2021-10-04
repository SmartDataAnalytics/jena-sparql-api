package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;

import org.aksw.commons.collection.observable.CollectionChangedEventImpl;
import org.aksw.commons.collection.observable.ObservableSet;
import org.aksw.jena_sparql_api.util.SetFromDatasetGraph;
import org.aksw.jena_sparql_api.util.SetFromGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class ObservableSetFromDatasetGraph
    extends SetFromDatasetGraph
    implements ObservableSet<Quad>
{
    //protected ObservableGraph graph;

    public ObservableSetFromDatasetGraph(ObservableDatasetGraph graph) {
        super(graph);
    }

    @Override
    public ObservableDatasetGraph getDatasetGraph() {
        return (ObservableDatasetGraph)super.getDatasetGraph();
    }

    //@Override
    //public boolean add(Triple t) {
    ////    Triple t = createTriple(node);
    //
    //    boolean result = !graph.contains(t);
    //
    //    if (result) {
    //        graph.add(t);
    //    }
    //    return result;
    //}

    protected PropertyChangeEvent convertEvent(PropertyChangeEvent ev) {
        CollectionChangedEventImpl<Quad> oldEvent = (CollectionChangedEventImpl<Quad>)ev;

        return new CollectionChangedEventImpl<Quad>(
            this,
            this,
            new SetFromGraph((Graph)oldEvent.getNewValue()),
            oldEvent.getAdditions(),
            oldEvent.getDeletions(),
            oldEvent.getRefreshes()
        );
    }


    /**
    *
    * @return A Runnable that de-registers the listener upon calling .run()
    */
    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
       return getDatasetGraph().addVetoableChangeListener(ev -> {
           PropertyChangeEvent newEvent = convertEvent(ev);
           listener.vetoableChange(newEvent);
       });
    }

    /**
     *
     * @return A Runnable that de-registers the listener upon calling .run()
     */
    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return getDatasetGraph().addPropertyChangeListener(ev -> {
            PropertyChangeEvent newEvent = convertEvent(ev);
            listener.propertyChange(newEvent);
        });
    }


    public static ObservableSetFromDatasetGraph decorate(DatasetGraph datasetGraph) {
        ObservableDatasetGraph tmp = ObservableDatasetGraphImpl.decorate(datasetGraph);
        ObservableSetFromDatasetGraph result = new ObservableSetFromDatasetGraph(tmp);
        return result;
    }

    @Override
    public boolean delta(Collection<? extends Quad> additions, Collection<?> removals) {
        throw new UnsupportedOperationException();
    }

}