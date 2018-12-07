package org.aksw.jena_sparql_api.core.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.SetFromGraph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateData;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class UpdateRequestUtils {

    /**
     * Append operations from src to tgt
     * @param tgt
     * @param src
     * @return
     */
    public static UpdateRequest append(UpdateRequest tgt, UpdateRequest src) {
        src.getOperations().forEach(tgt::add);
        return tgt;
    }

    public static UpdateRequest clone(UpdateRequest request) {
        UpdateRequest result = new UpdateRequest();
        result.setBaseURI(request.getBaseURI());
        result.setPrefixMapping(request.getPrefixMapping());
        result.setResolver(request.getResolver());

        for(Update update : request.getOperations()) {
            Update clone = UpdateUtils.clone(update);
            result.add(clone);
        }
        return result;
    }

    public static void applyWithIri(UpdateRequest updateRequest, String withIri) {
        for(Update update : updateRequest.getOperations()) {
            UpdateUtils.applyWithIriIfApplicable(update, withIri);
        }
    }

    public static void applyDatasetDescription(UpdateRequest updateRequest, DatasetDescription dg) {
        for(Update update : updateRequest.getOperations()) {
            UpdateUtils.applyDatasetDescriptionIfApplicable(update, dg);
        }
    }

    public static void fixVarNames(UpdateRequest updateRequest) {
        List<Update> updates = updateRequest.getOperations();

        for(Update update : updates) {
            if(update instanceof UpdateDeleteInsert) {
                UpdateDeleteInsert x = (UpdateDeleteInsert)update;
                Element before = x.getWherePattern();
                Element after = ElementUtils.fixVarNames(before);
                x.setElement(after);
            } else if(update instanceof UpdateModify) {
                UpdateModify x = (UpdateModify)update;
                Element before = x.getWherePattern();
                Element after = ElementUtils.fixVarNames(before);
                x.setElement(after);
            }
        }
    }


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
        Set<Triple> _a = added == null ? Collections.<Triple>emptySet() : SetFromGraph.wrap(added.getGraph());
        Set<Triple> _r = removed == null ? Collections.<Triple>emptySet() :  SetFromGraph.wrap(removed.getGraph());

        Iterable<Quad> a = Iterables.transform(_a, FN_QuadFromTriple.fnDefaultGraphNodeGenerated);
        Iterable<Quad> r = Iterables.transform(_r, FN_QuadFromTriple.fnDefaultGraphNodeGenerated);

        UpdateRequest result = createUpdateRequest(a, r);
        return result;
    }

    public static UpdateRequest createUpdateRequest(Iterable<? extends Quad> added, Iterable<? extends Quad> removed) {
        Iterator<? extends Quad> a = added == null ? null : added.iterator();
        Iterator<? extends Quad> b = removed == null ? null : removed.iterator();

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
