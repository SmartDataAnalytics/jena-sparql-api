package org.aksw.jena_sparql_api.path.core;

import java.util.List;

import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.apache.jena.sparql.path.P_Path0;


/** Path for SPARQL 1.1 property paths based on Jena's P_Path0 class */
public class PathPP
    extends PathBase<P_Path0, PathPP>
{
     public PathPP(PathOps<P_Path0, PathPP> pathOps, boolean isAbsolute, List<P_Path0> segments) {
        super(pathOps, isAbsolute, segments);
    }
}
