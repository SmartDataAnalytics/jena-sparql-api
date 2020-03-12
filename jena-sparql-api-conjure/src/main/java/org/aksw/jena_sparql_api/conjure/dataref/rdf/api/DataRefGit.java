package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefGit;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Reference to data stored in a git repository
 * Globbing is allowed 
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface DataRefGit
	extends PlainDataRefGit, DataRef
{
	@Iri("rpif:gitUrl")
	@IriType
	DataRefGit setGitUrl(String gitUrl);
	
	@Override
	@Iri("rpif:fileNamePatterns")
	List<String> getFileNamePatterns();
	DataRefGit setFileNamePatterns(List<String> fileNamePatterns);

	@Override
	default <T> T accept2(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

	public static DataRefGit create(Model model, String gitUrl, List<String> fileNamePatterns) {
		DataRefGit result = model.createResource().as(DataRefGit.class)
				.setGitUrl(gitUrl)
				.setFileNamePatterns(fileNamePatterns);

		return result;

	}
}
