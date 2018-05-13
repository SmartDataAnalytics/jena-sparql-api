package org.aksw.jena_sparql_api.mapper.core;

import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeCalendar;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.system.JenaSubsystemLifecycle;

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
