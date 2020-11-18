package org.aksw.dcat.jena.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
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
public interface MavenEntity
    extends Resource, MavenEntityCore
{
    @Iri("http://dataid.dbpedia.org/ns/core#group")
    String getGroupId();
    MavenEntity setGroupId(String groupId);

    @Iri("http://dataid.dbpedia.org/ns/core#artifact")
    String getArtifactId();
    MavenEntity setArtifactId(String artifactId);

    @IriNs("mvn")
    String getVersion();
    MavenEntity setVersion(String version);

    @IriNs("mvn")
    String getClassifier();
    MavenEntity setClassifier(String classifier);
}
