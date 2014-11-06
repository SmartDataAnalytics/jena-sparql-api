package org.aksw.jena_sparql_api.normal_form;

import java.util.Collection;

public class Dnf
	extends ExprNormalForm
{
	public Dnf(Collection<Clause> clauses) {
		super(clauses);
	}
}
