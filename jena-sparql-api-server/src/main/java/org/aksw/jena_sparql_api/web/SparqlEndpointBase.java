package org.aksw.jena_sparql_api.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.utils.SparqlFormatterUtils;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.out.SinkTripleOutput;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.Template;


/**
 * Jersey resource for an abstract SPARQL endpoint based on the AKSW SPARQL API.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
//@Produces("application/rdf+xml")
//@Produces("text/plain")
public abstract class SparqlEndpointBase {

	
	//private QueryExecutionFactory queryExecutionFactory;
	
	//public abstract QueryExecutionFactory getQueryExecutionFactory(@Context HttpServletRequest req);
	
	public abstract QueryExecutionStreaming createQueryExecution(Query query, @Context HttpServletRequest req);	
	
	//public abstract processQuery(@Context HttpServletRequest req, String queryString);
	
	/**
	 *  
	 * @param context The servlet context.
	 */
//	public SparqlEndpointBase(QueryExecutionFactory queryExecutionFactory) { //@Context ServletContext context) {
//		this.queryExecutionFactory = queryExecutionFactory;
//		//context.getAttribute("queryExecutionFactory");
//	}
	
	
	public static Set<Triple> getTriplesByVar(Var var, Collection<Triple> triples)
	{
		Set<Triple> result = new HashSet<Triple>();
		for(Triple triple : triples) {
			if(containsVar(triple, var)) {
				result.add(triple);
			}
		}
		
		return result;
	}
	
	public static Query selectToConstruct(Query query) {
		Set<Triple> triples = getTriples(query);
		BasicPattern bgp = new BasicPattern();
		
		for(Triple triple : triples) {
			bgp.add(triple);
		}
		Template template = new Template(bgp);
		
		Query result = new Query(query);
		result.setQueryConstructType();
		result.setConstructTemplate(template);
		
		return result;
/*		
		
		
		// For each result variable, get the triples that bind them
		List<String> vs = query.getResultVars();
		Set<Var> closedVars = new HashSet<Var>();
		Set<Var> openVars = new HashSet<Var>();
		for(String v : vs) {
			openVars.add(Var.alloc(v));
		}


		while(!openVars.isEmpty()) {
			
			Iterator<Var> it = openVars.iterator();
			Var var = it.next();
			it.remove();
			
			closedVars.add(var);
			
			Set<Triple> ts = getTriplesByVar(var, triples);
			
			for(Triple t : ts) {
				Set<Var>
			}
			
		}
		
*/
	}
	
	public static boolean containsVar(Triple triple, Var var) {
		boolean result
			= triple.getSubject().equals(var) 
			|| triple.getPredicate().equals(var) 
			|| triple.getObject().equals(var)
			;
		
		return result;
	}
	
	public static Set<Var> getVarsMentioned(Triple triple) {
		Set<Var> result = new HashSet<Var>();
	
		if(triple.getSubject().isVariable()) {
			result.add((Var)triple.getSubject());
		}
		else if(triple.getPredicate().isVariable()) {
			result.add((Var)triple.getPredicate());
		}
		else if(triple.getObject().isVariable()) {
			result.add((Var)triple.getObject());
		}
		
		return result;
	}
	
	public static Set<Triple> getTriples(Query query) {
		Element element = query.getQueryPattern();
		Set<Triple> result = getTriples(element);
		return result;
	}

	public static Set<Triple> getTriples(Element element) {
		Set<Triple> result = new HashSet<Triple>();
		getTriples(element, result);
		
		return result;
	}

	public static void getTriples(Element element, Collection<Triple> results) {
		if(element instanceof ElementFilter) {
			// Nothing Todo
		}
		else if(element instanceof ElementGroup) {
			ElementGroup e = (ElementGroup)element;
			
			for(Element item : e.getElements()) {
				getTriples(item, results);
			}
		}
		else if(element instanceof ElementOptional) {
			ElementOptional e = (ElementOptional)element;

			getTriples(e.getOptionalElement(), results);
		}
		else if(element instanceof ElementTriplesBlock) {
			ElementTriplesBlock e = (ElementTriplesBlock)element;

			BasicPattern bgp = e.getPattern();
			List<Triple> triples = bgp.getList();
			for(Triple triple : triples) {
				results.add(triple);
			}
		}
		else if(element instanceof ElementPathBlock) {
			ElementPathBlock e = (ElementPathBlock)element;
			for(TriplePath tp : e.getPattern().getList()) {
				Triple triple = tp.asTriple();
				results.add(triple);
			}
			
		} else {
			throw new RuntimeException("Do not know how to handle element: " + element.getClass() + " "+ element);
		}		
	}
	

	@GET
	@Path("/exportRdf")
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput exportRdf(@QueryParam("service-uri") String serviceUri, @QueryParam("query") String queryString)
			throws Exception
	{
		Query tmp = QueryFactory.create(queryString);
		Query query = selectToConstruct(tmp);
		query.setQueryPattern(tmp.getQueryPattern());
	
		System.out.println(query);
		
		QueryExecutionFactory qef = createQef(serviceUri);

		final QueryExecutionStreaming qe = qef.createQueryExecution(query);
		
		
		return new StreamingOutput() {

			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException {

				//Iterator<Triple> it = qe.execConstructStreaming();
				Model model = qe.execConstruct();
				Iterator<Triple> it = model.getGraph().find(null, null, null);
				Sink<Triple> sink = new SinkTripleOutput(out);
				
				while(it.hasNext()) {
					Triple triple = it.next();
					sink.send(triple);
				}
			}
		};
	}

	
	public static QueryExecutionFactory createQef(String serviceUri)
		throws Exception
	{
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(serviceUri);
		
		CacheCoreEx cacheBackend = CacheCoreH2.create("sparql",
				24l * 60l * 60l * 1000l, true);
		CacheEx cacheFrontend = new CacheExImpl(cacheBackend);		
		qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		qef = new QueryExecutionFactoryPaginated(qef, 1000);

		return qef;
		
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public StreamingOutput executeQueryXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {

		if(queryString == null) {
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	
	public StreamingOutput processQuery(HttpServletRequest req, String queryString, String format)
			throws Exception
	{
		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
		
		QueryExecutionStreaming qe = createQueryExecution(query, req);
		
		return ProcessQuery.processQuery(query, format, qe);
	}

	
	//@Produces(MediaType.APPLICATION_XML)
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public StreamingOutput executeQueryXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {

		if(queryString == null) {
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
	public StreamingOutput executeQueryJson(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
	public StreamingOutput executeQueryJsonPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
	}
	
	//@Produces("application/rdf+xml")
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@GET
	@Produces(HttpParams.contentTypeRDFXML)
	public StreamingOutput executeQueryRdfXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
	}	
	

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(HttpParams.contentTypeRDFXML)
	public StreamingOutput executeQueryRdfXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
	}

	@GET
	@Produces("application/sparql-results+xml")
	public StreamingOutput executeQueryResultSetXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/sparql-results+xml")
	public StreamingOutput executeQueryResultSetXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput executeQueryText(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Text);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput executeQueryTextPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Text);
	}

}

