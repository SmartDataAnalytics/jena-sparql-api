package org.aksw.jena_sparql_api.schema_mapping;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;

/**
 * A schema mapping holds the information for how to
 * construct a row in a target schema
 * based on a defining expression in term of a source schema.
 * 
 * @author Claus Stadler
 *
 */
public class SchemaMappingImpl
	implements SchemaMapping, Serializable
{
	private static final long serialVersionUID = 6530165559931064334L;

	protected Map<Var, FieldMapping> varToFieldMapping;
	
	public SchemaMappingImpl(Map<Var, FieldMapping> varToFieldMapping) {
		super();
		this.varToFieldMapping = varToFieldMapping;
	}

	@Override
	public Set<Var> getDefinedVars() {
		return varToFieldMapping.keySet();
	}

	@Override
	public Map<Var, FieldMapping> getFieldMapping() {
		return varToFieldMapping;
	}

	@Override
	public String toString() {
		Map<Var, FieldMapping> fieldMap = getFieldMapping();
		String result = fieldMap.entrySet().stream()
			.map(e -> "" + e.getKey() + ": " + e.getValue())
			.collect(Collectors.joining("\n"));
		
		return result;
	}
}
