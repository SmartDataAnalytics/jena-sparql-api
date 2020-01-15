package org.aksw.jena_sparql_api.conjure.datapod.api;

import java.util.List;

import org.aksw.dcat.ap.domain.api.Checksum;

/**
 * Bundles a datapod with hashes describing the content
 * 
 * @author raven
 *
 */
public interface RdfContentPod {
	List<Checksum> getContentHashes();
	DataPod getDataPod();
}
