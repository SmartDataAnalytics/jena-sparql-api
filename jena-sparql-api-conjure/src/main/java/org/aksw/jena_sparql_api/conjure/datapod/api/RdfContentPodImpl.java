package org.aksw.jena_sparql_api.conjure.datapod.api;

import java.util.List;

import org.aksw.dcat.ap.domain.api.Checksum;

public class RdfContentPodImpl
	implements RdfContentPod
{
	protected List<Checksum> contentHashes;
	protected RdfDataPod dataPod;
	
	public RdfContentPodImpl(List<Checksum> contentHashes, RdfDataPod dataPod) {
		super();
		this.contentHashes = contentHashes;
	}

	@Override
	public List<Checksum> getContentHashes() {
		return contentHashes;
	}

	@Override
	public DataPod getDataPod() {
		return dataPod;
	}

}
