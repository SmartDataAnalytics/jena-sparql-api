package org.aksw.jena_sparql_api.rx.entity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Core content metadata describing encoding, content type, charset and
 * language.
 *
 * @author raven
 *
 */
public interface EntityInfo {
    List<String> getContentEncodings();
    String getContentType();

    /**
     * Charset, such as UTF-8 or ISO 8859-1
     *
     * @return
     */
    String getCharset();

    /**
     * The set of language tags for which the content is suitable.
     *
     * @return
     */
    Set<String> getLanguageTags();


    /**
     * A set of IRIs for 'standards' (may be informal or ad-hoc) a resource conforms to
     *
     */
    Set<String> getConformsTo();

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
