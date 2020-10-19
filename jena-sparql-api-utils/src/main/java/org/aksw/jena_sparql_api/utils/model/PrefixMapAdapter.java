package org.aksw.jena_sparql_api.utils.model;

import java.util.Map;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapBase;
import org.apache.jena.shared.PrefixMapping;


/**
 * Wrapper to use a {@link PrefixMapping} in places where a {@link PrefixMap}
 * is required.
 *
 * @author raven
 *
 */
public class PrefixMapAdapter
    extends PrefixMapBase
{
    protected PrefixMapping prefixMapping;

    public PrefixMapAdapter(PrefixMapping prefixMapping) {
        super();
        this.prefixMapping = prefixMapping;
    }

    @Override
    public Map<String, String> getMapping() {
        return prefixMapping.getNsPrefixMap();
    }

    @Override
    public void add(String prefix, String iriString) {
        prefixMapping.setNsPrefix(prefix, iriString);
    }

    @Override
    public void delete(String prefix) {
        prefixMapping.removeNsPrefix(prefix);
    }

    @Override
    public void clear() {
        prefixMapping.clearNsPrefixMap();
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return prefixMapping.getNsPrefixURI(prefix) != null;
    }

    @Override
    public String abbreviate(String uriStr) {
        return prefixMapping.shortForm(uriStr);
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        Pair<String, String> result = null;
        String shortForm = prefixMapping.shortForm(uriStr);

        // Note: Contract of shortForm forbids null result
        if (shortForm != uriStr) {
            int splitPoint = shortForm.indexOf(':');
            if (splitPoint >= 0) {
                String prefix = shortForm.substring(0, splitPoint);

                // Validate the prefix for robustness:
                // The split may fail if a prefix already contained a colon
                if (prefixMapping.getNsPrefixURI(prefix) != null) {
                    String localName = shortForm.substring(splitPoint + 1);
                    result = Pair.create(prefix, localName);
                }
            }
        }

        return result;
    }

    @Override
    public String expand(String prefix, String localName) {
        String iri = prefixMapping.getNsPrefixURI(prefix);
        String result = iri != null
                ? iri + localName
                : null;

        return result;
    }

    @Override
    public boolean isEmpty() {
        return prefixMapping.hasNoMappings();
    }

    @Override
    public int size() {
        return prefixMapping.numPrefixes();
    }

    public static PrefixMap wrap(PrefixMapping prefixMapping) {
        return new PrefixMapAdapter(prefixMapping);
    }
}
