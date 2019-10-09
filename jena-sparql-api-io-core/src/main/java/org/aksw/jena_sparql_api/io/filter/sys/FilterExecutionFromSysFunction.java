package org.aksw.jena_sparql_api.io.filter.sys;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.io.endpoint.FilterExecution;
import org.aksw.jena_sparql_api.io.endpoint.FileWritingProcess;
import org.aksw.jena_sparql_api.io.endpoint.FilterConfig;

public class FilterExecutionFromSysFunction 
	implements FilterConfig
{
	protected SysCallFn cmdFactory;
	protected FilterExecution source;

	protected FilterExecutionFromSysFunction(SysCallFn cmdFactory, FilterExecution source) {
		this.cmdFactory = cmdFactory;
		this.source = source;
	}

	@Override
	public InputStream execStream() throws IOException {
	
	}
	
	
	

}
