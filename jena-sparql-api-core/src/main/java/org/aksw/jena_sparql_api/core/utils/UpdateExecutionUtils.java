package org.aksw.jena_sparql_api.core.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.QuadContainmentChecker;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.utils.DatasetGraphDiffUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateExecutionUtils {

    private static final Logger logger = LoggerFactory.getLogger(UpdateExecutionUtils.class);

    /**
     * Copy data from one sparql service to another based on a construct query
     *
     * @param target
     * @param source
     * @param constructQueryStr
     * @param batchSize
     */
    public static void copyByConstruct(SparqlService target, SparqlService source, String constructQueryStr, int batchSize) {
        Query query = QueryFactory.create(constructQueryStr, "http://example.org/", Syntax.syntaxARQ);

        QueryExecution qe = source.getQueryExecutionFactory().createQueryExecution(query);
        Iterator<Triple> it = qe.execConstructTriples();

        Iterator<List<Triple>> itPart = Iterators.partition(it, batchSize);

        while(itPart.hasNext()) {
            List<Triple> part = itPart.next();
            logger.debug("Items in this chunk: " + part.size());
            executeInsertTriples(target.getUpdateExecutionFactory(), part);
        }
        logger.debug("Done with this chunk");
    }

    public static void executeUpdate(SparqlService sparqlService, String requestStr, int batchSize, Iterable<DatasetListener> listeners, QuadContainmentChecker containmentChecker) { //, Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter) {
        //UpdateRequest updateRequest = new UpdateRequest();
        //UpdateFactory.parse(updateRequest, requestStr);
        UpdateRequest updateRequest = UpdateRequestUtils.parse(requestStr);
        executeUpdate(sparqlService, updateRequest, batchSize, listeners, containmentChecker);
    }

    public static void executeUpdate(SparqlService sparqlService, UpdateRequest request, int batchSize, Iterable<DatasetListener> listeners, QuadContainmentChecker containmentChecker) { //Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

        Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter = new FN_QuadDiffUnique(qef, containmentChecker);


        for(Update update : request.getOperations()) {
            executeUpdateCore(qef, uef, update, filter, batchSize, listeners);
        }
    }


    public static void executeUpdateCore(
            QueryExecutionFactory qef,
            UpdateExecutionFactory uef,
            Update update,
            Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter,
            int batchSize,
            Iterable<DatasetListener> listeners)
    {
        String withIri = UpdateUtils.getWithIri(update);

        Iterator<Diff<Set<Quad>>> itDiff = UpdateDiffUtils.createIteratorDiff(qef, update, batchSize);

        while(itDiff.hasNext()) {
            Diff<Set<Quad>> diff = itDiff.next();

            Diff<Set<Quad>> filteredDiff = filter != null
                    ? filter.apply(diff)
                    : diff;

            if(listeners != null) {
                DatasetListenerUtils.notifyListeners(listeners, filteredDiff, null);
            }

            executeUpdate(uef, diff);
        }
    }

    public static UpdateProcessor executeInsertTriples(UpdateExecutionFactory uef, Iterable<Triple> triples) {
        Iterable<Quad> quads = Iterables.transform(triples, FN_QuadFromTriple.fnDefaultGraphNodeGenerated);
        UpdateProcessor result = executeInsertQuads(uef, quads);
        return result;
    }

    public static UpdateProcessor executeDeleteTriples(UpdateExecutionFactory uef, Iterable<Triple> triples) {
        Iterable<Quad> quads = Iterables.transform(triples, FN_QuadFromTriple.fnDefaultGraphNodeGenerated);
        UpdateProcessor result = executeDeleteQuads(uef, quads);
        return result;
    }

    public static UpdateProcessor executeUpdateQuads(UpdateExecutionFactory uef, Iterable<? extends Quad> quads, boolean isDelete) {
        UpdateProcessor result = isDelete
                ? executeDeleteQuads(uef, quads)
                : executeInsertQuads(uef, quads)
                ;
        return result;
    }

    public static UpdateProcessor executeInsertQuads(UpdateExecutionFactory uef, Iterable<? extends Quad> quads) {
        UpdateRequest updateRequest = UpdateRequestUtils.createUpdateRequest(quads, Collections.<Quad>emptySet());
        UpdateProcessor result = executeUnlessEmpty(uef, updateRequest);
        return result;
    }

    public static UpdateProcessor executeDeleteQuads(UpdateExecutionFactory uef, Iterable<? extends Quad> quads) {
        UpdateRequest updateRequest = UpdateRequestUtils.createUpdateRequest(Collections.<Quad>emptySet(), quads);
        UpdateProcessor result = executeUnlessEmpty(uef, updateRequest);
        return result;
    }

    public static UpdateProcessor executeUpdate(UpdateExecutionFactory uef, Diff<? extends Iterable<? extends Quad>> diff) {
        UpdateRequest updateRequest = UpdateRequestUtils.createUpdateRequest(diff);
        UpdateProcessor result = executeUnlessEmpty(uef, updateRequest);
        return result;
    }

    public static UpdateProcessor executeUpdateDatasetGraph(UpdateExecutionFactory uef, Diff<? extends DatasetGraph> diff) {
        Diff<Set<Quad>> d = DatasetGraphDiffUtils.wrapDatasetGraph(diff);

        UpdateRequest updateRequest = UpdateRequestUtils.createUpdateRequest(d);
        UpdateProcessor result = executeUnlessEmpty(uef, updateRequest);
        return result;
    }

    public static UpdateProcessor executeUnlessEmpty(UpdateExecutionFactory uef, UpdateRequest updateRequest) {
        UpdateProcessor result;
        if(updateRequest.getOperations().isEmpty()) {
            // Create a fake update request
            UpdateRequest update = UpdateFactory.create("PREFIX ex: <http://example.org/> INSERT { ex:s ex:p ex:o } WHERE { ex:s ex:p ex:o }");
            result = com.hp.hpl.jena.update.UpdateExecutionFactory.create(update, GraphStoreFactory.create(ModelFactory.createDefaultModel()));
            result.execute();
        } else {
            result = uef.createUpdateProcessor(updateRequest);
            result.execute();
        }

        return result;
    }
}
