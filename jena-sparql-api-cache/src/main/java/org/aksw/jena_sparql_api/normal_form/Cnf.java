package org.aksw.jena_sparql_api.normal_form;

import java.util.Collection;

public class Cnf
	extends ExprNormalForm
{
	public Cnf(Collection<Clause> clauses) {
		super(clauses);
	}
}
