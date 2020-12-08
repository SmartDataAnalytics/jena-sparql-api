package org.aksw.dcat.jena.domain.api;

public interface MavenEntityCore {
    String getGroupId();
    MavenEntityCore setGroupId(String groupId);

    String getArtifactId();
    MavenEntityCore setArtifactId(String artifactId);

    String getVersion();
    MavenEntityCore setVersion(String version);

    String getClassifier();
    MavenEntityCore setClassifier(String classifier);
}
