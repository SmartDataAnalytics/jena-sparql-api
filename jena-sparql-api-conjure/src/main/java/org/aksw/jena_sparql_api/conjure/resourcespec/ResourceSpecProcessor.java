package org.aksw.jena_sparql_api.conjure.resourcespec;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;


public class ResourceSpecProcessor
	implements ResourceSpecVisitor<InputStream>
{
	@Override
	public InputStream visit(ResourceSpecUrl dataRef) {
		String filenameOrURI = dataRef.getResourceUrl();
		InputStream result = SparqlStmtUtils.openInputStream(filenameOrURI);
		return result;
	}

	@Override
	public InputStream visit(ResourceSpecInline dataRef) {
		String value = dataRef.setValue();
		Objects.nonNull(value);
		
		InputStream result = new ByteArrayInputStream(value.getBytes());
		return result;
	}

}
