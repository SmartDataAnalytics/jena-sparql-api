package org.aksw.jena_sparql_api.changeset;

import org.apache.jena.graph.Graph;

public class ChangeSet {
    private ChangeSetMetadata metadata;

    private String uri;
    private String precedingChangeSet;

    private String subjectOfChange;
    private Graph addition;
    private Graph removal;

    /**
     * Extension of ChangeSets for dealing with multiple services and graphs
     */
    private String service;
    private String graph;

    //private Map<String, String> changeReason = new HashMap<String, String>();//HashMultimap.create();

    public ChangeSet(ChangeSetMetadata metadata, String uri,
            String precedingChangeSet, String subjectOfChange, Graph addition,
            Graph removal) {
        this(metadata, uri, precedingChangeSet, subjectOfChange, addition, removal, null, null);
    }

    public ChangeSet(ChangeSetMetadata metadata, String uri,
            String precedingChangeSet, String subjectOfChange, Graph addition,
            Graph removal, String service, String graph) {
        super();
        this.metadata = metadata;
        this.uri = uri;
        this.precedingChangeSet = precedingChangeSet;
        this.subjectOfChange = subjectOfChange;
        this.addition = addition;
        this.removal = removal;
        this.service = service;
        this.graph = graph;
    }


    public String getUri() {
        return uri;
    }

    public String getSubjectOfChange() {
        return subjectOfChange;
    }

    public Graph getAddition() {
        return addition;
    }

    public Graph getRemoval() {
        return removal;
    }

    public ChangeSetMetadata getMetadata() {
        return metadata;
    }

    public String getPrecedingChangeSet() {
        return precedingChangeSet;
    }

    public String getService() {
        return service;
    }

    public String getGraph() {
        return graph;
    }


    public boolean isEmpty() {
        boolean result = addition.isEmpty() && removal.isEmpty();
        return result;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((addition == null) ? 0 : addition.hashCode());
        result = prime * result + ((graph == null) ? 0 : graph.hashCode());
        result = prime * result
                + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + ((precedingChangeSet == null) ? 0
                : precedingChangeSet.hashCode());
        result = prime * result + ((removal == null) ? 0 : removal.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        result = prime * result
                + ((subjectOfChange == null) ? 0 : subjectOfChange.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChangeSet other = (ChangeSet) obj;
        if (addition == null) {
            if (other.addition != null)
                return false;
        } else if (!addition.equals(other.addition))
            return false;
        if (graph == null) {
            if (other.graph != null)
                return false;
        } else if (!graph.equals(other.graph))
            return false;
        if (metadata == null) {
            if (other.metadata != null)
                return false;
        } else if (!metadata.equals(other.metadata))
            return false;
        if (precedingChangeSet == null) {
            if (other.precedingChangeSet != null)
                return false;
        } else if (!precedingChangeSet.equals(other.precedingChangeSet))
            return false;
        if (removal == null) {
            if (other.removal != null)
                return false;
        } else if (!removal.equals(other.removal))
            return false;
        if (service == null) {
            if (other.service != null)
                return false;
        } else if (!service.equals(other.service))
            return false;
        if (subjectOfChange == null) {
            if (other.subjectOfChange != null)
                return false;
        } else if (!subjectOfChange.equals(other.subjectOfChange))
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChangeSet [metadata=" + metadata + ", uri=" + uri
                + ", precedingChangeSet=" + precedingChangeSet
                + ", subjectOfChange=" + subjectOfChange + ", addition="
                + addition + ", removal=" + removal + ", service=" + service
                + ", graph=" + graph + "]";
    }

}
