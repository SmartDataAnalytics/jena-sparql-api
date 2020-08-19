package org.aksw.jena_sparql_api.rx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;

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
    public static Set<String> getLangNames(Lang lang) {
        Set<String> result = new HashSet<>();
        result.add(lang.getName());
        result.addAll(lang.getAltNames());
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
        return getLangNames(lang).stream()
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
