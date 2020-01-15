package org.aksw.jena_sparql_api.http.repository.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface EntityInfoCore {
	List<String> getContentEncodings();
	//EntityInfoCore setEncodings(List<String> encodings);
	
	String getContentType();
	//EntityInfoCore setContentType(String contentType);
	
	Long getContentLength();
	//EntityInfoCore setContentLength(Long length);
	
	/**
	 * Charset, such as UTF-8 or ISO 8859-1
	 * 
	 * @return
	 */
	String getCharset();
	EntityInfoCore setCharset(String charset);

	/**
	 * The set of language tags for which the content is suitable.
	 * 
	 * @return
	 */
	Set<String> getLanguageTags();
	EntityInfoCore setLanguages(Set<String> languageTags);
	
	
	/**
	 * Convenience method
	 * 
	 * @return
	 */
	default String getEncodingsAsHttpHeader() {
		String result = getContentEncodings().stream().collect(Collectors.joining(","));
		return result;
	}
}
