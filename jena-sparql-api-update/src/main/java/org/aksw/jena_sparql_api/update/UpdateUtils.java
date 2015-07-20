package org.aksw.jena_sparql_api.update;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateData;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataDelete;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.sparql.modify.request.UpdateModify;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class UpdateUtils {

    public static Iterator<Diff<Set<Quad>>> createIteratorDiff(Iterator<? extends Iterable<? extends Binding>> itBindings, Diff<? extends Iterable<Quad>> quadDiff) {
        FunctionDiffFromBindings fn = FunctionDiffFromBindings.create(quadDiff);
        Iterator<Diff<Set<Quad>>> result = Iterators.transform(itBindings, fn);
        return result;
    }

    public static Diff<Set<Quad>> buildDiff(Iterable<? extends Binding> bindings, Diff<? extends Iterable<Quad>> quadDiff) {
        Set<Quad> inserts = new HashSet<Quad>();
        Set<Quad> deletes = new HashSet<Quad>();

        for(Binding binding : bindings) {

            Set<Quad> i = QueryUtils.instanciate(quadDiff.getAdded(), binding);
            Set<Quad> d = QueryUtils.instanciate(quadDiff.getRemoved(), binding);

            inserts.addAll(i);
            deletes.addAll(d);
            // TODO Deal with overlaps
        }

        Diff<Set<Quad>> result = new Diff<Set<Quad>>(inserts, deletes, null);
        return result;
    }

    public static <T> Set<T> asSet(Iterable<T> c)
    {
        return (c instanceof Set) ? (Set<T>)c : Sets.newHashSet(c);
    }

    public static Diff<Set<Quad>> makeUnique(Diff<? extends Iterable<Quad>> diff, QueryExecutionFactory qef, QuadContainmentChecker quadContainmentChecker) {

        Set<Quad> added = asSet(diff.getAdded());
        Set<Quad> removed = asSet(diff.getRemoved());

        added = added == null ? Collections.<Quad>emptySet() : added;
        removed = removed == null ? Collections.<Quad>emptySet() : removed;

        Set<Quad> toCheck = new HashSet<Quad>();
        toCheck.addAll(added);
        toCheck.addAll(removed);

        Set<Quad> containedQuads = quadContainmentChecker.contains(qef, toCheck);

        Set<Quad> actualAdded = Sets.difference(added, containedQuads); // added minus containedQuads
        Set<Quad> actualRemoved = Sets.intersection(removed, containedQuads); // removed intersected with containedQuads

        Diff<Set<Quad>> result = new Diff<Set<Quad>>(actualAdded, actualRemoved, null);

        return result;
    }


    public static Iterator<Diff<Set<Quad>>> createIteratorDiff(QueryExecutionFactory qef, Update update, int batchSize) {
        Iterator<Diff<Set<Quad>>> result;

        if(update instanceof UpdateModify) {
            result = createIteratorDiff(qef, (UpdateModify)update, batchSize);
        } else if(update instanceof UpdateDataInsert) {
            result = createIteratorDiff(qef, (UpdateDataInsert)update);
        } else if(update instanceof UpdateDataDelete) {
            result = createIteratorDiff(qef, (UpdateDataDelete)update);
        } else {
            throw new RuntimeException("Unsupported update type: " + update.getClass());
        }

        return result;
    }

    public static Iterator<Diff<Set<Quad>>> createIteratorDiff(QueryExecutionFactory qef, UpdateModify update, int batchSize) {

        Element wherePattern = update.getWherePattern();
        Query query = QueryUtils.elementToQuery(wherePattern);

        // TODO Limit and offset...
        QueryExecution qe = qef.createQueryExecution(query);
        ExtendedIterator<Binding> itBinding = ResultSetUtils.toIteratorBinding(qe);

        Iterator<List<Binding>> itBindingChunk = Iterators.partition(itBinding, batchSize);

        Diff<List<Quad>> template = new Diff<List<Quad>>(update.getInsertQuads(), update.getDeleteQuads(), null);
        Iterator<Diff<Set<Quad>>> result = createIteratorDiff(itBindingChunk, template);
        //QuadDiffIterator result = new QuadDiffIterator(itBindingChunk, template);

        return result;
    }

    public static Iterator<Diff<Set<Quad>>> createIteratorDiff(QueryExecutionFactory qef, UpdateDataInsert update)
    {
        Diff<Set<Quad>> diff = new Diff<Set<Quad>>(Sets.newHashSet(update.getQuads()), Sets.<Quad>newHashSet(), null);

        Iterator<Diff<Set<Quad>>> result = Collections.singleton(diff).iterator();
        return result;
    }

    public static Iterator<Diff<Set<Quad>>> createIteratorDiff(QueryExecutionFactory qef, UpdateDataDelete update)
    {
        Diff<Set<Quad>> diff = new Diff<Set<Quad>>(Sets.<Quad>newHashSet(), Sets.newHashSet(update.getQuads()), null);

        Iterator<Diff<Set<Quad>>> result = Collections.singleton(diff).iterator();
        return result;
    }


    public static UpdateRequest createUpdateRequest(Diff<? extends Iterable<Quad>> diff)
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


    public static UpdateRequest createUpdateRequest(Iterable<Quad> added, Iterable<Quad> removed) {
        UpdateRequest result = new UpdateRequest();

        if(added != null && !Iterables.isEmpty(added)) {
            QuadDataAcc insertQuads = new QuadDataAcc(Lists.newArrayList(added));
            UpdateData insertData = new UpdateDataInsert(insertQuads);
            result.add(insertData);
        }

        if(removed != null && !Iterables.isEmpty(removed)) {
            QuadDataAcc deleteQuads = new QuadDataAcc(Lists.newArrayList(removed));
            UpdateData deleteData = new UpdateDataDelete(deleteQuads);
            result.add(deleteData);
        }

        return result;
    }

    public static UpdateProcessor executeUpdate(UpdateExecutionFactory uef, Diff<? extends Iterable<Quad>> diff) {
        UpdateRequest updateRequest = createUpdateRequest(diff);
        UpdateProcessor result = uef.createUpdateProcessor(updateRequest);
        result.execute();
        return result;
    }

    public static UpdateRequest parse(String requestStr) {
        UpdateRequest result = new UpdateRequest();
        UpdateFactory.parse(result, requestStr);

        return result;
    }

    //UpdateExecutionFactory uef, QueryExecutionFactory qef
    public static void executeUpdate(SparqlService sparqlService, String requestStr, int batchSize, Iterable<DatasetListener> listeners, QuadContainmentChecker containmentChecker) { //, Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter) {
        //UpdateRequest updateRequest = new UpdateRequest();
        //UpdateFactory.parse(updateRequest, requestStr);
        UpdateRequest updateRequest = parse(requestStr);
        executeUpdate(sparqlService, updateRequest, batchSize, listeners, containmentChecker);
    }

    public static void executeUpdate(SparqlService sparqlService, UpdateRequest request, int batchSize, Iterable<DatasetListener> listeners, QuadContainmentChecker containmentChecker) { //Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

        Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter = new FunctionQuadDiffUnique(qef, containmentChecker);


        for(Update update : request.getOperations()) {
            Iterator<Diff<Set<Quad>>> itDiff = createIteratorDiff(qef, update, batchSize);

            while(itDiff.hasNext()) {
                Diff<Set<Quad>> diff = itDiff.next();

                Diff<Set<Quad>> filteredDiff = filter != null
                        ? filter.apply(diff)
                        : diff;

                if(listeners != null) {
                    notifyListeners(listeners, filteredDiff, null);
                }

                executeUpdate(uef, diff);
            }
        }
    }

    public static void notifyListeners(Iterable<DatasetListener> listeners, Diff<Set<Quad>> diff, UpdateContext updateContext) {
        for(DatasetListener listener : listeners) {
            listener.onPreModify(diff, updateContext);
        }
    }

}
