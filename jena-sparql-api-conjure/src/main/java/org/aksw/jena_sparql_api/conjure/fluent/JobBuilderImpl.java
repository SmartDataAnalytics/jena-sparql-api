package org.aksw.jena_sparql_api.conjure.fluent;

public class JobBuilderImpl
	implements JobBuilder
{
	protected ConjureContext context;
	
	public JobBuilderImpl() {
		this(new ConjureContext());
	}

	public JobBuilderImpl(ConjureContext context) {
		super();
		this.context = context;
	}

}
