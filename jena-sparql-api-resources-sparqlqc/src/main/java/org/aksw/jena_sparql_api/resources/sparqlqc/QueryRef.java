package org.aksw.jena_sparql_api.resources.sparqlqc;

public class QueryRef {
	protected long id;
	protected String variant;

	public QueryRef(long id, String variant) {
		super();
		this.id = id;
		this.variant = variant;
	}

	public long getId() {
		return id;
	}

	public String getVariant() {
		return variant;
	}
}
