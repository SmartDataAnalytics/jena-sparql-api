package org.aksw.jena_sparql_api.conjure.datapod.impl;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;

public interface RdfDataPodHdt
	extends RdfDataPod
{
	/**
	 * DataPods backed HDT-like formats can allow access to the header pod
	 * TODO What is the header pod of a header? Either an empty model or null
	 * 
	 * @return
	 */
	RdfDataPod headerPod();
}
