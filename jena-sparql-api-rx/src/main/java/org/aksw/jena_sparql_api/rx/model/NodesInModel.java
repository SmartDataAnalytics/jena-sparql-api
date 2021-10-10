package org.aksw.jena_sparql_api.rx.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import io.reactivex.rxjava3.core.Flowable;

public class NodesInModel {
    protected NodeSelector nodeSelector;
    protected Model model;

    public NodesInModel(Model model, NodeSelector nodeSelector) {
        super();
        this.model = model;
        this.nodeSelector = nodeSelector;
    }

    public Model getModel() {
        return model;
    }

    public NodeSelector getNodeSelector() {
        return nodeSelector;
    }

    public Flowable<RDFNode> getRDFNodes() {
        return nodeSelector.streamRDFNodes(model);
    }
}
