package org.aksw.jena_sparql_api.rx.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import io.reactivex.rxjava3.core.Flowable;

public interface NodeSelector {
    Flowable<RDFNode> streamRDFNodes(Model model);
}
