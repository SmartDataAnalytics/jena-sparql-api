package org.aksw.jena_sparql_api.rx.io.resultset;

/**
 * Depending on the arguments the determined main output type is among this enum.
 * See also the {@link OutputModes#detectOutputMode(java.util.Collection)} method.
 *
 * @author raven
 *
 */
public enum OutputMode
{
    UNKOWN,
    QUAD,
    TRIPLE,
    BINDING,
    JSON
}