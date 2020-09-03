package org.aksw.jena_sparql_api.rx;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;

/**
 * Convenience methods related to Jena's {@link RDFLanguages} class.
 *
 * @author raven
 *
 */
public class RDFLanguagesEx {

    /**
     * Returns quad langs first followed by the triple ones.
     * Returned langs are distinct.
     *
     * @return
     */
    public static List<Lang> getQuadAndTripleLangs() {
        List<Lang> result = Stream.concat(getQuadLangs().stream(), getTripleLangs().stream())
                .distinct()
                .collect(Collectors.toList());

        return result;
    }

    public static List<Lang> getResultSetLangs() {
        List<Lang> result = RDFLanguages.getRegisteredLanguages().stream()
                .filter(ResultSetReaderRegistry::isRegistered)
                .collect(Collectors.toList());

        return result;
    }

    public static List<Lang> getTripleLangs() {
        List<Lang> result = RDFLanguages.getRegisteredLanguages().stream()
            .filter(RDFLanguages::isTriples)
            .collect(Collectors.toList());

        return result;
    }

    public static List<Lang> getQuadLangs() {
        List<Lang> result = RDFLanguages.getRegisteredLanguages().stream()
            .filter(RDFLanguages::isQuads)
            .collect(Collectors.toList());

        return result;
    }

    /**
     * Get the set of preferred and alternative labels from a given lang object
     *
     * @param lang
     * @return
     */
    public static Set<String> getAllLangNames(Lang lang) {
        Set<String> result = new LinkedHashSet<>();
        result.add(lang.getName());
        result.addAll(lang.getAltNames());
        return result;
    }

    /**
     * Get the set of preferred and alternative content types from a given lang object
     *
     * @param lang
     * @return
     */
    public static Set<String> getAllContentTypes(Lang lang) {
        Set<String> result = new LinkedHashSet<>();
        ContentType primaryCt = lang.getContentType();
        if(primaryCt != null) {
            result.add(primaryCt.toHeaderString());
        }

        result.addAll(lang.getAltContentTypes());
        return result;
    }

    /**
     * Simple helper to check whether any of a lang's labels match a given one.
     * Returns the first match
     *
     * @param lang
     * @param label
     * @return
     */
    public static boolean matchesLang(Lang lang, String label) {
        return getAllLangNames(lang).stream()
            .anyMatch(name -> name.equalsIgnoreCase(label));
    }

    /**
     * Find the first RDFFormat that matches a given label
     *
     * @param label
     * @return
     */
    public static RDFFormat findRdfFormat(String label) {
        RDFFormat outFormat = RDFWriterRegistry.registered().stream()
                .filter(fmt -> fmt.toString().equalsIgnoreCase(label) || matchesLang(fmt.getLang(), label))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No RDF format found for label " + label));

        return outFormat;
    }



//	public static RDFFormat findLang(String label) {
//		RDFFormat outFormat = RDFLanguages.fi.registered().stream()
//				.filter(fmt -> fmt.toString().equalsIgnoreCase(label) || matchesLang(fmt.getLang(), label))
//				.findFirst()
//				.orElseThrow(() -> new RuntimeException("No RDF format found for label " + label));
//
//		return outFormat;
//	}

}
