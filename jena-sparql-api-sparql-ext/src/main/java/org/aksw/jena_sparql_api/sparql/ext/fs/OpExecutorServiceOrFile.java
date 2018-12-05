package org.aksw.jena_sparql_api.sparql.ext.fs;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;

public class OpExecutorServiceOrFile
	extends OpExecutor
{	
	protected OpExecutorServiceOrFile(ExecutionContext execCxt) {
	    super(execCxt);	
	}
	
	@Override
	protected QueryIterator execute(OpService opService, QueryIterator input) {
		return new QueryIterServiceOrFile(input, opService, execCxt);
	}
}
