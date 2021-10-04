package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import org.apache.jena.sparql.core.DatasetGraph;

public interface ObservableDatasetGraph
	extends DatasetGraph
{
    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Runnable addPropertyChangeListener(PropertyChangeListener listener);
}
