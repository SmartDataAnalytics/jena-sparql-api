package org.aksw.jena_sparql_api.conjure.resourcespec;

import java.io.IOException;
import java.util.Set;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSpecUtils {
	private static final Logger logger = LoggerFactory.getLogger(ResourceSpecUtils.class);

	/**
	 * Find references to placeholders in a model and resolve them
	 * 
	 * @param model
	 * @throws IOException 
	 */
	public static Model resolve(Model model) throws IOException {
		
		Set<Resource> cands = model.listResourcesWithProperty(RPIF.resourceUrl).toSet();
	
		for(Resource cand : cands) {
			Set<Statement> stmts = ResourceUtils.listReverseProperties(cand, null).toSet();

			for(Statement stmt : stmts) {	
				Resource s = stmt.getSubject();
				Property p = stmt.getPredicate();
				Resource o = stmt.getObject().asResource();
				
				//o.getProperty(RPIF.resourceUrl).getString();
				String str = ResourceUtils.getLiteralPropertyValue(o, RPIF.resourceUrl, String.class);
				String replacement = SparqlStmtUtils.loadString(str);
				
				logger.trace("Mapped reference " + str + " to " + replacement);
				
				s.removeAll(p);
				o.removeProperties();
				s.addProperty(p, replacement);
			}
		}
		
		return model;
	}	
}
