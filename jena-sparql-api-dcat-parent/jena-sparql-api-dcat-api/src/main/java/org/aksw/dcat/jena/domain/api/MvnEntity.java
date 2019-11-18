package org.aksw.dcat.jena.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

/**
 * An interface with some very useful properties for identification and versioning
 * of arbitrary artifacts
 * 
 * @author raven
 *
 */
@ResourceView
public interface MvnEntity
	extends Resource
{
	@IriNs("mvn")
	String getGroupId();
	MvnEntity setGroupId(String groupId);
	
	@IriNs("mvn")
	String getArtifactId();
	MvnEntity setArtifactId(String artifactId);
	
	@IriNs("mvn")
	String getVersion();
	MvnEntity setVersion(String version);
	
	@IriNs("mvn")
	String getClassifier();
	MvnEntity setClassifier(String classifier);
}
