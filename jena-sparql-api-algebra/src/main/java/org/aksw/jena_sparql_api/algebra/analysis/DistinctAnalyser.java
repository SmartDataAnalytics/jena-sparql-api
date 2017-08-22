package org.aksw.jena_sparql_api.algebra.analysis;

/**
 * Performs a top down traversal of an algebra tree and labels
 * nodes with which sets of variables are allowed to be distinct
 *
 * DISTINCT(PROJECT ?s (UNION(A, B)))
 *
 * -> A: DISTINCT ?s
 * -> B: DISTINCT ?s
 *
 *
 *
 *
 *
 * @author raven
 *
 */
public class DistinctAnalyser
  //implements V
{


}
