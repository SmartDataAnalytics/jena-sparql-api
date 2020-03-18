package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import java.util.List;

public interface PlainDataRefGit
	extends PlainDataRef
{
	String getGitUrl();
	List<String> getFileNamePatterns();
	
	@Override
	default <T> T accept(PlainDataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
