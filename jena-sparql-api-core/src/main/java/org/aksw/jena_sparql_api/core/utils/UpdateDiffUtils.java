package org.aksw.jena_sparql_api.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QuadContainmentChecker;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.util.SetFromDatasetGraph;
import org.aksw.jena_sparql_api.utils.DatasetGraphDiffUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.update.Update;
import org.apache.jena.util.iterator.ExtendedIterator;

public class UpdateDiffUtils {

    public static Diff<DatasetGraph> combineDatasetGraph(Iterable<? extends Diff<? extends DatasetGraph>> diffs) {
        Diff<DatasetGraph> result = Diff.create(DatasetGraphFactory.createGeneral(), DatasetGraphFactory.createGeneral());

        // Create a writable view on the result
        Diff<Set<Quad>> resultView = DatasetGraphDiffUtils.wrapDatasetGraph(result);

        for(Diff<? extends DatasetGraph> diff : diffs) {
            Diff<Set<Quad>> itemView = DatasetGraphDiffUtils.wrapDatasetGraph(diff);
            combine(resultView, itemView);
        }

        return result;
    }


    public static <T> void combine(Diff<? extends Collection<T>> target, Diff<? extends Iterable<T>> source) {
        Iterables.addAll(target.getAdded(), source.getAdded());
        Iterables.addAll(target.getRemoved(), source.getRemoved());
    }

    public static Diff<Set<Quad>> combineIterables(Iterable<? extends Diff<? extends Iterable<Quad>>> diffs) {
        Diff<Set<Quad>> result = Diff.<Set<Quad>>create(new HashSet<Quad>(), new HashSet<Quad>());
        for(Diff<? extends Iterable<Quad>> diff : diffs) {
            combine(result, diff);
        }

        return result;
    }

    public static Diff<Set<Quad>> makeUnique(Diff<? extends Iterable<Quad>> diff, QueryExecutionFactory qef, QuadContainmentChecker quadContainmentChecker) {

        Set<Quad> added = SetUtils.asSet(diff.getAdded());
        Set<Quad> removed = SetUtils.asSet(diff.getRemoved());

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
        } else if(update instanceof UpdateDeleteWhere) {
            result = createIteratorDiff(qef, (UpdateDeleteWhere)update, batchSize);
        } else {
            throw new RuntimeException("Unsupported update type: " + update.getClass());
        }

        return result;
    }


    public static Iterator<Diff<Set<Quad>>> createIteratorDiff(QueryExecutionFactory qef, UpdateDeleteWhere update, int batchSize) {

        UpdateModify tmp = new UpdateModify();
        QuadAcc acc = tmp.getDeleteAcc();

        for(Quad quad : update.getQuads()) {
            acc.addQuad(quad);
        }

        Element element = QuadUtils.toElement(acc.getQuads());
        tmp.setElement(element);

        Iterator<Diff<Set<Quad>>> result = createIteratorDiff(qef, tmp, batchSize);
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

    public static Iterator<Diff<Set<Quad>>> createIteratorDiff(Iterator<? extends Iterable<? extends Binding>> itBindings, Diff<? extends Iterable<Quad>> quadDiff) {
        FN_DiffFromBindings fn = FN_DiffFromBindings.create(quadDiff);
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

    public static Diff<Set<Quad>> computeDelta(DatasetGraph after, DatasetGraph before) {
        SetFromDatasetGraph afterSet = new SetFromDatasetGraph(after);
        SetFromDatasetGraph beforeSet = new SetFromDatasetGraph(before);

        Diff<Set<Quad>> result = computeDelta(afterSet, beforeSet);
        return result;
    }

    public static Diff<Set<Quad>> computeDelta(Set<Quad> after, Set<Quad> before) {
        Set<Quad> actualAdded = Sets.difference(after, before);
        Set<Quad> actualRemoved = Sets.difference(before, after);

        Diff<Set<Quad>> result = new Diff<Set<Quad>>(actualAdded, actualRemoved, null);
        return result;
    }
}
