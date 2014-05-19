package org.aksw.jena_sparql_api.sparql_path.core;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Map1;

class Map1StatementToObject
	implements Map1<Statement, Resource>
{
	@Override
	public Resource map1(Statement stmt) {
		return stmt.getObject().asResource();
	}	
}