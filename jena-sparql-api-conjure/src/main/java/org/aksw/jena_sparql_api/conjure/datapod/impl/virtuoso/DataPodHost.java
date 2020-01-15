package org.aksw.jena_sparql_api.conjure.datapod.impl.virtuoso;

import org.aksw.jena_sparql_api.conjure.datapod.api.DataPod;

public interface DataPodHost {
	DataPod aquireDataPod(String name);
}
