package org.aksw.jena_sparql_api.conjure.datapod.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;

public interface DataPodFactory {
	DataPod create(PlainDataRef dataRef);
}
