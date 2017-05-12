package fr.inrialpes.tyrexmo.testqc.simple;

/**
 * Simple string based interface for containment checks
 *
 * @author raven
 *
 */
public interface SimpleContainmentSolver {
    public boolean entailed(String queryStr1, String queryStr2);

    public boolean entailedUnderSchema(String schema, String queryStr1, String queryStr2);
}
