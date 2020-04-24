package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.io.binseach.GraphFromPrefixMatcher;
import org.aksw.jena_sparql_api.rx.GraphOpsRx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.RDFLanguagesEx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.UriUtils;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.iterator.QueryIterService;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * TODO Factory out into a more general class that delegates each bindings to custom processor
 *
 * @author Claus Stadler, Dec 5, 2018
 *
 */
public class QueryIterServiceOrFile extends QueryIterService {

    protected OpService opService ;

    public QueryIterServiceOrFile(QueryIterator input, OpService opService, ExecutionContext context) {
        super(input, opService, context);

        // TODO Sigh, Jena made this attribute package visible only...
        this.opService = opService;
    }

    public static Path toPath(Node node) {
        Entry<Path, Map<String, String>> tmp = toPathSpec(node);
        Path result = tmp.getKey();
        return result;
    }

    public static Entry<Path, Map<String, String>> toPathSpec(Node node) {
        Entry<Path, Map<String, String>> result = null;
        if(node.isURI()) {
            String uriStr = node.getURI();

            boolean isFileRef = uriStr.startsWith("file:");
            if(isFileRef) {
                Path path;
                try {
                    URI uri = new URI(uriStr);
                    Map<String, String> params = UriUtils.createMapFromUriQueryString(uri);

                    // Cut off any query string
                    URI effectiveUri = new URI(uriStr.replaceAll("\\?.*", ""));

                    path = Paths.get(effectiveUri);
                    boolean fileExists = Files.exists(path);

                    result = fileExists ? Maps.immutableEntry(path, params) : null;
                } catch (URISyntaxException e) {
                    //throw new RuntimeException(e);
                    // Nothing todo; we simply return null if we fail
                }
            }
        }

        return result;
    }


    @Override
    protected QueryIterator nextStage(Binding outerBinding)
    {
        OpService op = (OpService)QC.substitute(opService, outerBinding);

        Node serviceNode = op.getService();

        //Path path = toPath(serviceNode);
        Entry<Path, Map<String, String>> fileSpec = toPathSpec(serviceNode);

        QueryIterator result = fileSpec == null
                ? super.nextStage(outerBinding)//nextStageService(outerBinding)
                : nextStagePath(outerBinding, fileSpec.getKey(), fileSpec.getValue());

        return result;
    }


    protected QueryIterator nextStagePath(Binding outerBinding, Path path, Map<String, String> params) //Path path)
    {
        OpService op = (OpService)QC.substitute(opService, outerBinding);
        boolean silent = opService.getSilent() ;
        QueryIterator qIter ;
        try {
            Op subOp = op.getSubOp();
            Query query = OpAsQuery.asQuery(subOp);

            //QueryIterator right;
            Iterator<Binding> it = null;

            boolean specialProcessingApplied = false;

            // TODO Try to parse out options from the service string, such as
            // file?binarySearch=true
            //String uriQs = uri.getQuery();
            //List<NameValuePair> nm = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

            String streamVal = params.get("stream");
            if(!Strings.isNullOrEmpty(streamVal)) {
                if("s".equalsIgnoreCase(streamVal)) {
                    specialProcessingApplied = true;

                    // Stream by subject - useful for answering star patterns
                    List<Lang> tripleLangs = RDFLanguagesEx.getTripleLangs();
                    TypedInputStream tmp = RDFDataMgrEx.open(path.toString(), tripleLangs);

                    Flowable<Binding> flow = RDFDataMgrRx.createFlowableTriples(() -> tmp)
                            .compose(GraphOpsRx.groupConsecutiveTriplesByComponent(Triple::getSubject, GraphFactory::createDefaultGraph))
                            .map(ModelFactory::createModelForGraph)
                            .parallel()
                            .flatMap(m ->
                                SparqlRx.execSelectRaw(() -> QueryExecutionFactory.create(query.cloneQuery(), m)))
                            .sequential();

                    it = flow
                            .blockingIterable().iterator();
                } else {
                    throw new RuntimeException("For streaming in SERVICE, only 's' for subjects is presently supported.");
                }
            }

            String binSearchVal = params.get("binsearch");
            QueryExecution qe;
            if("true".equalsIgnoreCase(binSearchVal)) {
                specialProcessingApplied = true;

                Graph graph = new GraphFromPrefixMatcher(path);
                Model model = ModelFactory.createModelForGraph(graph);
                // qe = QueryExecutionFactory.create(query, model);//, input);
                // right = new QueryIteratorResultSet(qe.execSelect());
                it = SparqlRx.execSelectRaw(() -> QueryExecutionFactory.create(query, model))
                        .blockingIterable().iterator();
            }

            if(!specialProcessingApplied) {
                String url = path.toUri().toString();
                Dataset dataset = RDFDataMgr.loadDataset(url);

//    	    	// TODO Probably add namespaces declared on query scope (how to access them?)
                //query.addGraphURI(path.toUri().toString());

//                qe = QueryExecutionFactory.create(query, dataset);//, input);
//                right = new QueryIteratorResultSet(qe.execSelect());
                it = SparqlRx.execSelectRaw(() -> QueryExecutionFactory.create(query, dataset))
                        .blockingIterable().iterator();
            }

            Iterator<Binding> tmp = it;
            QueryIterator right = new QueryIteratorBindingIterator(it) {
                @Override
                protected final void requestCancel() {
                    ((Disposable)tmp).dispose();
                }
            };


            // This iterator is materialized already otherwise we may end up
            // not servicing the HTTP connection as needed.
            // In extremis, can cause a deadlock when SERVICE loops back to this server.
            // Add tracking.
            qIter = QueryIter.makeTracked(right, getExecContext()) ;
        } catch (RuntimeException ex)
        {
            if ( silent )
            {
                Log.warn(this, "SERVICE <" + opService.getService().toString() + ">: " + ex.getMessage()) ;
                // Return the input
                return QueryIterSingleton.create(outerBinding, getExecContext()) ;
            }
            throw ex ;
        }

        // Need to put the outerBinding as parent to every binding of the service call.
        // There should be no variables in common because of the OpSubstitute.substitute
        QueryIterator qIter2 = new QueryIterCommonParent(qIter, outerBinding, getExecContext()) ;
        return qIter2 ;
    }
}