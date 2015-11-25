package org.aksw.jena_sparql_api.batch;

/**
 * Phase 1: Counting...
 * Phase 2: 123/456 Datasets exported.
 *
 */
public class ExportProgress {
    boolean isRunning;
    boolean isFinished;
    boolean isSuccess; // Only valid, if isFinished is true

    private String message;

    // True if export is in counting phase
    boolean isCounting;

    // Counted triples
    long maxTripleCount;

    // Current state of the export
    long currentTripleCount;

}