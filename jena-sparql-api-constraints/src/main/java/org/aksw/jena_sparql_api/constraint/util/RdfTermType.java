package org.aksw.jena_sparql_api.constraint.util;

/**
 * RDF term types
 *
 * @author raven
 *
 */
public enum RdfTermType {
    UNKNOWN (0x01),
    IRI     (0x02),
    BNODE   (0x04),
    LITERAL (0x08);

    private final int value;

    private RdfTermType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
