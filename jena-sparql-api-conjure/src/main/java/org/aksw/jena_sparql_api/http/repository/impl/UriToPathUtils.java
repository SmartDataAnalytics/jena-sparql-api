package org.aksw.jena_sparql_api.http.repository.impl;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

public class UriToPathUtils {

    public static String hostNameToPath(String hostName) {
        String[] parts = hostName.split("\\.");
        ArrayUtils.reverse(parts);

        String result = Arrays.asList(parts).stream().collect(Collectors.joining("/"));
//        Path result = Paths.get(str);
        return result;
    }

    /**
     * Default mapping of URIs to relative paths
     *
     * scheme://host:port/path?query becomes
     * host/port/path/query
     *
     * @param uri
     * @return
     */
    public static Path resolvePath(URI uri) {
        return resolvePath(uri, true);
    }

    public static Path resolvePath(URI uri, boolean hostNameToPath) {
        String a = Optional.ofNullable(uri.getHost())
                .map(str -> hostNameToPath ? hostNameToPath(str) : str)
                .orElse("");

        String b = uri.getPort() == -1 ? "" : Integer.toString(uri.getPort());

        // Replace ~ (tilde) with _ because otherwise jena IRI validation will fail
        // on file:// urls with SCHEME_PATTERN_MATCH_FAILED
        // Tilde is common symbol with e.g. the Apache Web server's userdir mod
        String pathStr =  Optional.ofNullable(uri.getPath()).orElse("")
                .replaceAll("~", "_");

        Path result = Paths.get(".")
        .resolve(a)
        .resolve(b)
        .resolve((a.isEmpty() && b.isEmpty() ? "" : ".") + pathStr)
        .resolve(Optional.ofNullable(uri.getQuery()).orElse(""))
        .normalize();

        return result;
    }

    public static Path resolvePath(String uri)  {
        return resolvePath(uri, true);
    }

    public static Path resolvePath(String uri, boolean hostNameToPath)  {
        URI u = URIUtils.newURI(uri);

        Path tmp = u == null ?
            Paths.get(StringUtils.urlEncode(uri))
            : UriToPathUtils.resolvePath(u);

        // Make absolute paths relative (i.e. remove leading slashes)
        Path result;
        if(tmp.isAbsolute()) {
            Path root = tmp.getRoot();
            result = root.relativize(tmp);
        } else {
            result = tmp;
        }

        //logger.info("Resolved: " + uri + "\n  to: " + result + "\n  via: " + u);
        return result;
    }

}
