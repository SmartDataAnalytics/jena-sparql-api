package org.aksw.jena_sparql_api.sparql_path.core;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.Map1;

class Map1StatementToObject
    implements Map1<Statement, Resource>
{
    @Override
    public Resource apply(Statement stmt) {
        return stmt.getObject().asResource();
    }
}