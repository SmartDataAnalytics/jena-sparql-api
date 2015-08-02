package org.aksw.jena_sparql_api.core;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;

import com.hp.hpl.jena.sparql.core.Quad;

public interface DatasetListener {
    /**
     * Event notifying about a modification to an RDF dataset, i.e. a set of quads being added or removed.
     * @param diff
     */
    void onPreModify(Diff<Set<Quad>> diff, UpdateContext updateContext);

    //void onPreDelete(Iterator<Quad> it);
    //void onPreInsert(Iterator<Quad> it);
}
