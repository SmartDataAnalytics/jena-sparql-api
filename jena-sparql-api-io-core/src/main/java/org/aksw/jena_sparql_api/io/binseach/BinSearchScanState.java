package org.aksw.jena_sparql_api.io.binseach;


/**
 * State information for scanning a region once binary search has found an offset.
 * The end of the region is detected dynamically
 *
 * @author raven
 *
 */
public class BinSearchScanState {
    long size;
    long firstDelimPos;
    long matchDelimPos;
    byte[] prefixBytes;
}