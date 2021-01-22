package org.aksw.jena_sparql_api.schema_mapping;

import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public interface SchemaMapping {
	Set<Var> getDefinedVars();
	
	Map<Var, FieldMapping> getFieldMapping();
}
