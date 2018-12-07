package org.aksw.jena_sparql_api.mapper.core;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitJenaSparqlApiMapper
	implements JenaSubsystemLifecycle
{
	public void start() {
		//TypeMapper.getInstance().registerDatatype(new RDFDatatypeCalendar());
	}

	@Override
	public void stop() {
	}
}
