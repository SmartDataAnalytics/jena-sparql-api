package org.aksw.dcat.jena.domain.api;

public interface MvnEntityCore {
    String getGroupId();
    MvnEntityCore setGroupId(String groupId);

    String getArtifactId();
    MvnEntityCore setArtifactId(String artifactId);

    String getVersion();
    MvnEntityCore setVersion(String version);

    String getClassifier();
    MvnEntityCore setClassifier(String classifier);
}
