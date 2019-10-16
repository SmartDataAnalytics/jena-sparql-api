package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.iterator.QueryIterService;

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
    	Path result = null;
    	if(node.isURI()) {
		    String uri = node.getURI();
		
		    boolean isFileRef = uri.startsWith("file:");
		    if(isFileRef) {
				Path path;
		    	try {
					path = Paths.get(new URI(uri));
				    boolean fileExists = Files.exists(path);
				    
				    result = fileExists ? path : null;
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
        
        Path path = toPath(serviceNode);
        
        QueryIterator result = path == null
        		? super.nextStage(outerBinding)//nextStageService(outerBinding)
        		: nextStagePath(outerBinding, path);
        		
        return result;
    }
    

    protected QueryIterator nextStagePath(Binding outerBinding, Path path)
    {
        OpService op = (OpService)QC.substitute(opService, outerBinding);
        boolean silent = opService.getSilent() ;
        QueryIterator qIter ;
        try {
        	String url = path.toUri().toString();
        	Dataset dataset = RDFDataMgr.loadDataset(url);
        	
//	    	// TODO Probably add namespaces declared on query scope (how to access them?)
	        Op subOp = op.getSubOp();
	        Query query = OpAsQuery.asQuery(subOp);
	        //query.addGraphURI(path.toUri().toString());
	        
	        QueryExecution qe = QueryExecutionFactory.create(query, dataset);//, input);
	        QueryIterator right = new QueryIteratorResultSet(qe.execSelect());
	        
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