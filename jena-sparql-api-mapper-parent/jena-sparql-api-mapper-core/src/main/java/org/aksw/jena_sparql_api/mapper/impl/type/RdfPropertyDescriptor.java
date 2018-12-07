package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfType;

public class RdfPropertyDescriptor {
	protected String name;
	protected RdfType rdfType;
	protected String fetchMode;

	public RdfPropertyDescriptor(String name, RdfType rdfType, String fetchMode) {
		super();
		this.name = name;
		this.rdfType = rdfType;
		this.fetchMode = fetchMode;
	}

	public String getName() {
		return name;
	}



	public RdfType getRdfType() {
		return rdfType;
	}

	public String getFetchMode() {
		return fetchMode;
	}


}
