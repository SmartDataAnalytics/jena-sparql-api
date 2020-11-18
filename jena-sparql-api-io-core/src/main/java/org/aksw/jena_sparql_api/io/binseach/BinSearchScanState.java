package org.aksw.jena_sparql_api.io.binseach;


/**
 * State information for scanning a region once binary search has found an offset.
 * The end of the region is detected dynamically
 *
 * TODO Generalize to arbitrary patterns
 *
 * @author raven
 *
 */
public class BinSearchScanState {
    long size;          // Absolute end of the data region on which the match was run
    long matchDelimPos; // The match position found by binary search
    long firstDelimPos; // The match position found by scanning backwards with Pattern.match
    byte[] prefixBytes; // Generalize using lambda with a compatible signature to Pattern.match
}