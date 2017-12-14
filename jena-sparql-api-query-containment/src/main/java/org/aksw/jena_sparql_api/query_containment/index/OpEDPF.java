package org.aksw.jena_sparql_api.query_containment.index;

/**
 * Op for combined
 *
 * Extend, Distinct, Project, Filter
 *
 * Issue: Distinct could be applied before or after the extend -
 * how to handle this???
 *
 * So the order is
 * Distinct
 * Extend / Project (this is a map, and each target variable maps to its defining expression; possibly itself)
 * Filter
 *
 *
 * @author raven
 *
 */
public class OpEDPF {
    //protected
}
