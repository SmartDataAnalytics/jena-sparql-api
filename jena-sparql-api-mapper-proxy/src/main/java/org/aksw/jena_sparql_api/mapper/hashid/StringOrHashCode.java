package org.aksw.jena_sparql_api.mapper.hashid;


/**
 * Although hash codes are ultimately represented as an array of bytes, they are
 * more restrictive than strings and rather represent numbers,
 * whereas strings should represent <b>url-safe</b> sequences of characters
 *
 * @author raven
 *
 */
public interface StringOrHashCode {
    boolean isHashCode();
    boolean isString();



    // If the object already represents a HashCode it is returned as is
    // Strings are converted to hash codes via the provided hash function
    // HashCode toHashCode(HashFunction hashFunction);
    String toString();
}
