package org.aksw.jena_sparql_api.rx;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.sys.JenaSystem;

/**
 * Convenience methods related to Jena's {@link RDFLanguages} class.
 *
 * @author raven
 *
 */
public class RDFLanguagesEx {

    static { JenaSystem.init(); }

    // public static Collection<Lang> basicQuadLangs = Arrays.asList(Lang.TRIG, Lang.NQUADS)

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

//    public static <T> void makeLast(Collection<T> collection, Collection<? extends T> items) {
//        for (T item : items) {
//            if (collection.contains(item)) {
//                collection.remove(item);
//                collection.add(item);
//            }
//        }
//    }

    /**
     * Util function that filters out result set langs that are not suitable for probing
     * For example CSV accepts nearly any kind of input
     *
     * @return
     */
    public static List<Lang> getResultSetProbeLangs() {
        List<Lang> result = RDFLanguagesEx.getResultSetLangs();
        result.remove(ResultSetLang.SPARQLResultSetCSV);
        return result;
    }

    /**
     * Get all registered result set languages
     *
     * @return
     */
    public static List<Lang> getResultSetLangs() {
        List<Lang> result = RDFLanguages.getRegisteredLanguages().stream()
                .filter(ResultSetReaderRegistry::isRegistered)
                .collect(Collectors.toList());


        // Sort the last resort languages last
//        List<Lang> lastResort = Arrays.asList(ResultSetLang.SPARQLResultSetText, ResultSetLang.SPARQLResultSetTSV);
//        makeLast(result, lastResort);

        return result;
    }

    /**
     * Return the languages available for writing result sets out.
     * For some reason at least in Jena 3.15.0 the text format is not
     * explicitly registered in the writer registry although it can be used - this seems to be a small bug so
     *
     *
     *
     */
    public static List<Lang> getResultSetFormats() {
        List<Lang> result = RDFLanguages.getRegisteredLanguages().stream()
                .filter(ResultSetWriterRegistry::isRegistered)
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
            result.add(primaryCt.getContentTypeStr());
        }

        lang.getAltContentTypes().stream().filter(Objects::nonNull).forEach(result::add);
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

    public static boolean matchesFileExtension(Lang lang, String label) {
        return lang.getFileExtensions().stream()
            .anyMatch(name -> name.equalsIgnoreCase(label));
    }

    public static boolean matchesContentType(Lang lang, String label) {
        // Extra robustness with null handling to deal with potentially broken registrations
        return getAllContentTypes(lang).stream()
            .anyMatch(contentType -> contentType.equalsIgnoreCase(label));
    }


    /**
     * Find the first RDFFormat that matches a given label
     *
     * @param label
     * @return
     */
    public static RDFFormat findRdfFormat(String label) {
        RDFFormat outFormat = findRdfFormat(label, RDFWriterRegistry.registered());
        return outFormat;
    }


    public static RDFFormat findRdfFormat(String label, Collection<RDFFormat> probeFormats) {
        RDFFormat outFormat = probeFormats.stream()
                .filter(fmt -> fmt.toString().equalsIgnoreCase(label)
                        || matchesLang(fmt.getLang(), label)
                        || matchesContentType(fmt.getLang(), label))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No RDF format found for label " + label));

        return outFormat;
    }


    public static Lang findLang(String label) {
        Lang result = findLang(label, RDFLanguages.getRegisteredLanguages());
        return result;
    }

    public static Lang findLang(String label, Collection<Lang> probeLangs) {
        Lang result = probeLangs.stream()
                .filter(lang -> matchesLang(lang, label)
                        || matchesFileExtension(lang, label)
                        || matchesContentType(lang, label))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No lang found for label " + label));

        return result;
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
