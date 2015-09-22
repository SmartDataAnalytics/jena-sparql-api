package org.aksw.jena_sparql_api.core.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.utils.SetGraph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateData;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataDelete;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateRequestUtils {
    public static UpdateRequest createUpdateRequestDatasetGraph(Diff<? extends DatasetGraph> diff)
    {
    	Iterator<Quad> a = diff.getAdded().find();
    	Iterator<Quad> b = diff.getRemoved().find();

    	UpdateRequest result = createUpdateRequest(a, b);
    	return result;
    }

    public static UpdateRequest createUpdateRequest(Diff<? extends Iterable<? extends Quad>> diff)
    {
        UpdateRequest result = createUpdateRequest(diff.getAdded(), diff.getRemoved());
        return result;
    }

    public static UpdateRequest createUpdateRequest(Model added, Model removed)
    {
        Set<Triple> _a = added == null ? Collections.<Triple>emptySet() : SetGraph.wrap(added.getGraph());
        Set<Triple> _r = removed == null ? Collections.<Triple>emptySet() :  SetGraph.wrap(removed.getGraph());

        Iterable<Quad> a = Iterables.transform(_a, FN_QuadFromTriple.fnDefaultGraphNodeGenerated);
        Iterable<Quad> r = Iterables.transform(_r, FN_QuadFromTriple.fnDefaultGraphNodeGenerated);

        UpdateRequest result = createUpdateRequest(a, r);
        return result;
    }

    public static UpdateRequest createUpdateRequest(Iterable<? extends Quad> added, Iterable<? extends Quad> removed) {
    	Iterator<? extends Quad> a = added.iterator();
    	Iterator<? extends Quad> b = removed.iterator();

    	UpdateRequest result = createUpdateRequest(a, b);
    	return result;
    }

    public static UpdateRequest createUpdateRequest(Iterator<? extends Quad> added, Iterator<? extends Quad> removed) {
        UpdateRequest result = new UpdateRequest();

        if(added != null && added.hasNext()) {
            QuadDataAcc insertQuads = new QuadDataAcc(Lists.newArrayList(added));
            UpdateData insertData = new UpdateDataInsert(insertQuads);
            result.add(insertData);
        }

        if(removed != null && removed.hasNext()) {
            QuadDataAcc deleteQuads = new QuadDataAcc(Lists.newArrayList(removed));
            UpdateData deleteData = new UpdateDataDelete(deleteQuads);
            result.add(deleteData);
        }

        return result;
    }

    public static UpdateRequest parse(String requestStr) {
        UpdateRequest result = new UpdateRequest();
        UpdateFactory.parse(result, requestStr);

        return result;
    }

}
