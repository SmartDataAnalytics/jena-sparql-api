package org.aksw.jena_sparql_api.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.rdf.model.impl.Util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * This class does not really belong to the SPARQL api,
 * but still it is useful for the server component.
 *
 * Code originated from a post on stackoverflow (forgot to note the link)
 *
 * @author Claus Stadler
 *
 */
public class UriUtils {

    /**
     * See {@link #replaceNamespace}
     */
    public static final Pattern replaceNamespacePattern = Pattern.compile("(?<=/)[^/]+(?=/[^/]+/*$)");


    /**
     * Only retains first value
     * @return
     */
    public static Map<String, String> createMapFromUriQueryString(URI uri) {
        return createMapFromUriQueryString(uri, new LinkedHashMap<>());
    }

    /**
     * Only retains first value
     * @return
     */
    public static Map<String, String> createMapFromUriQueryString(URI uri, Map<String, String> result) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        for (NameValuePair pair : pairs) {
            result.putIfAbsent(pair.getName(), pair.getValue());
        }

        return result;
    }


    /**
     * Taken from Node_URI.getNameSpace()
     *
     * @return
     */
    public static String getNameSpace(String s) {
        return s.substring( 0, Util.splitNamespaceXML( s ) );
    }

    /**
     * Taken from Node_URI.getLocalName()
     *
     * @return
     */
    public static String getLocalName(String s) {
        return s.substring( Util.splitNamespaceXML( s ) );
    }

    /**
     * http://example.org/foo/bar
     * becomes
     * http://example.org/baz/bar
     *
     *
     * @param base
     * @param replacement
     * @return
     */
    public static String replaceNamespace(String base, String replacement) {
        Matcher m = replaceNamespacePattern.matcher(base);
        String result = m.replaceAll(replacement);

        //String result = base.replace("([^/]+)/([^/]+)$", replacement);
        return result;
    }


    public static Multimap<String, String> parseQueryString(String queryString) {
        try {
            return parseQueryStringEx(queryString);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Multimap<String, String> parseQueryStringEx(String queryString)
            throws UnsupportedEncodingException
    {
        Multimap<String, String> result = ArrayListMultimap.create();

        if(queryString == null) {
            return result;
        }

        for (String param : queryString.split("&")) {
            String pair[] = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = "";
            if (pair.length > 1) {
                value = URLDecoder.decode(pair[1], "UTF-8");
            }
            result.put(new String(key), new String(value));
        }

        return result;
    }

}
