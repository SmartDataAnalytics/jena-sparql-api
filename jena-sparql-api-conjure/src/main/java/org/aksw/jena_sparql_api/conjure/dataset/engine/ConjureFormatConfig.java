package org.aksw.jena_sparql_api.conjure.dataset.engine;

import org.apache.jena.riot.RDFFormat;

public class ConjureFormatConfig {
	protected RDFFormat provenanceFormat;
	protected RDFFormat datasetFormat;
	protected RDFFormat catalogFormat;

	public ConjureFormatConfig() {
		super();
		this.provenanceFormat = RDFFormat.TURTLE_PRETTY;
		this.datasetFormat = RDFFormat.TURTLE_PRETTY;
		this.catalogFormat = RDFFormat.TURTLE_PRETTY;
	}
	
	public ConjureFormatConfig(RDFFormat provenanceFormat, RDFFormat datasetFormat, RDFFormat catalogFormat) {
		super();
		this.provenanceFormat = provenanceFormat;
		this.datasetFormat = datasetFormat;
		this.catalogFormat = catalogFormat;
	}

	public RDFFormat getProvenanceFormat() {
		return provenanceFormat;
	}
	public void setProvenanceFormat(RDFFormat provenanceFormat) {
		this.provenanceFormat = provenanceFormat;
	}
	public RDFFormat getDatasetFormat() {
		return datasetFormat;
	}
	public void setDatasetFormat(RDFFormat datasetFormat) {
		this.datasetFormat = datasetFormat;
	}
	public RDFFormat getCatalogFormat() {
		return catalogFormat;
	}
	public void setCatalogFormat(RDFFormat catalogFormat) {
		this.catalogFormat = catalogFormat;
	}
	
	
}
