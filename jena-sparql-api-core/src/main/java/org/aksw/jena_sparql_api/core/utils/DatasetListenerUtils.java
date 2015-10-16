package org.aksw.jena_sparql_api.core.utils;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.UpdateContext;

import com.hp.hpl.jena.sparql.core.Quad;

public class DatasetListenerUtils {
    public static void notifyListeners(Iterable<DatasetListener> listeners, Diff<Set<Quad>> diff, SparqlServiceReference serviceRef, UpdateContext updateContext) {
        for(DatasetListener listener : listeners) {
            listener.onPreModify(diff, serviceRef, updateContext);
        }
    }
}
