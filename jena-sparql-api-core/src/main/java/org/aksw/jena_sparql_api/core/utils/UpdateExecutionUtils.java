package org.aksw.jena_sparql_api.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.QuadContainmentChecker;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateContext;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.http.HttpExceptionUtils;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.DatasetGraphDiffUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

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

    public static UpdateProcessor executeInsert(UpdateExecutionFactory uef, Model model) {
        Graph graph = model.getGraph();
        UpdateProcessor result = executeInsert(uef, graph);
        return result;
    }

    public static UpdateProcessor executeInsert(UpdateExecutionFactory uef, Graph graph) {
        //ExtendedIterator<Triple> it = graph.find(null, null, null);
        Set<Triple> triples = graph.find(null, null, null).toSet();
        UpdateProcessor result = executeInsertTriples(uef, triples);

        //UpdateProcessor result = executeInsertIterator(uef, it, 1000);
        return result;
    }

//    public static UpdateProcessor executeInsertIterator(UpdateExecutionFactory uef, Iterator<Triple> tripleIt, int chunksize)
//    {
//
//        try {
//            Iterator<List<Triple>> it = Iterators.partition(tripleIt, chunksize);
//            while(it.hasNext()) {
//                List<Triple> triples = it.next();
//                executeInsertTriples(uef, triples);
//            }
//        } finally {
//            if(tripleIt instanceof ExtendedIterator) {
//                ExtendedIterator<?> tmp = (ExtendedIterator<?>)tripleIt;
//                tmp.close();
//            }
//        }
//    }


    public static void executeUpdate(SparqlService sparqlService, String requestStr, int batchSize, QuadContainmentChecker containmentChecker, Iterable<DatasetListener> listeners) { //, Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter) {
        //UpdateRequest updateRequest = new UpdateRequest();
        //UpdateFactory.parse(updateRequest, requestStr);
        UpdateRequest updateRequest = UpdateRequestUtils.parse(requestStr);
        executeUpdate(sparqlService, updateRequest, batchSize, containmentChecker, listeners);
    }

    public static void executeUpdate(SparqlService sparqlService, UpdateRequest request, int batchSize, QuadContainmentChecker containmentChecker, Iterable<DatasetListener> listeners) { //Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
//        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

        Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter = new FN_QuadDiffUnique(qef, containmentChecker);

        //SparqlServiceReference ssr = sparqlService.getDatasetDescription();

        for(Update update : request.getOperations()) {
            executeUpdateCore(sparqlService, update, filter, batchSize, listeners);
        }
    }

    public static String extractWithIri(SparqlService sparqlService, Update update) {
        String result = UpdateUtils.getWithIri(update);
        if(result == null) {
            DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
            result = DatasetDescriptionUtils.getSingleDefaultGraphUri(datasetDescription);
        }
        return result;
    }

    public static void executeUpdateCore(
            SparqlService sparqlService,
            Update update,
            Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter,
            int batchSize,
            Iterable<DatasetListener> listeners)
    {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

        Node with = NodeUtils.asNullableNode(extractWithIri(sparqlService, update));
        if(with == null) {
            throw new RuntimeException("No target graph for updates could be identified; i.e. no with uri or single default graph specified. " + update);
        }

        Iterator<Diff<Set<Quad>>> itDiffRaw = UpdateDiffUtils.createIteratorDiff(qef, update, batchSize);

        Map<Node, Node> map = new HashMap<Node, Node>();
        map.put(Quad.defaultGraphIRI, with);
        map.put(Quad.defaultGraphNodeGenerated, with);
        final NodeTransform nodeTransform = new NodeTransformRenameMap(map);
        Iterator<Diff<Set<Quad>>> itDiff = Iterators.transform(itDiffRaw, new Function<Diff<Set<Quad>>, Diff<Set<Quad>>>() {

            @Override
            public Diff<Set<Quad>> apply(Diff<Set<Quad>> input) {

                Set<Quad> added = QuadUtils.applyNodeTransform(input.getAdded(), nodeTransform);
                Set<Quad> removed = QuadUtils.applyNodeTransform(input.getRemoved(), nodeTransform);

                Diff<Set<Quad>> r = Diff.create(added, removed);
                return r;
            }
        });




        while(itDiff.hasNext()) {
            Diff<Set<Quad>> diff = itDiff.next();

            Diff<Set<Quad>> filteredDiff = filter != null
                    ? filter.apply(diff)
                    : diff;

            if(listeners != null) {
                UpdateContext updateContext = new UpdateContext(sparqlService, batchSize, null);
                DatasetListenerUtils.notifyListeners(listeners, filteredDiff, updateContext);
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
            UpdateRequest update = UpdateFactory.create("PREFIX ex: <http://example.org/> INSERT { ex:_s ex:_p ex:_o } WHERE { ex:_s ex:_p ex:_o }");
            result = org.apache.jena.update.UpdateExecutionFactory.create(update, GraphStoreFactory.create(ModelFactory.createDefaultModel()));
            result.execute();
        } else {
            result = uef.createUpdateProcessor(updateRequest);
            try {
                result.execute();
            } catch(Exception e) {
                RuntimeException f = HttpExceptionUtils.makeHumanFriendly(e);
                throw f;
            }
        }

        return result;
    }

    public static UpdateProcessor executeUpdateDelta(UpdateExecutionFactory uef, DatasetGraph after, DatasetGraph before) {
        Diff<Set<Quad>> diff = UpdateDiffUtils.computeDelta(after, before);
        UpdateProcessor result = executeUpdate(uef, diff);
        return result;
    }


    public static UpdateDeleteInsert createUpdateRename(Node before, Node after, int i) {
        Node[] deleteTerms = { Quad.defaultGraphIRI, Vars.s, Vars.p, Vars.o };
        deleteTerms[i] = before;
        Quad deleteQuad = QuadUtils.arrayToQuad(deleteTerms);

        Node[] insertTerms = { Quad.defaultGraphIRI, Vars.s, Vars.p, Vars.o };
        insertTerms[i] = after;
        Quad insertQuad = QuadUtils.arrayToQuad(insertTerms);

        UpdateDeleteInsert result = new UpdateDeleteInsert();
        result.getDeleteAcc().addQuad(deleteQuad);
        result.getInsertAcc().addQuad(insertQuad);

        result.setElement(ElementUtils.createElement(deleteQuad));

        return result;
    }

    /**
     * DELETE { &lt;s&gt; ?p ?o } INSERT { &lt;x&gt; ?p ?o} WHERE { &lt;s&gt; ?p ?o }
     *
     * @param uef
     * @param before
     * @param after
     * @return
     */
    public static UpdateRequest createUpdateRequestRename(Node before, Node after) {
        UpdateRequest result = new UpdateRequest();

        Update g = createUpdateRename(before, after, 0);
        Update s = createUpdateRename(before, after, 1);
        Update p = createUpdateRename(before, after, 2);
        Update o = createUpdateRename(before, after, 3);

        result.add(g);
        result.add(s);
        result.add(p);
        result.add(o);

        return result;
    }


    public static UpdateProcessor executeUpdateRename(UpdateExecutionFactory uef, Node before, Node after) {
        UpdateRequest updateRequest = createUpdateRequestRename(before, after);
        UpdateProcessor result = executeUnlessEmpty(uef, updateRequest);

        return result;
    }

//    public static Diff<Set<Quad>> createDiff(Set<Quad> after, Set<Quad> before) {
//    }
}
