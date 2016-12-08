package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.view_matcher.QueryToGraph;

public class ContainmentSolverWrapperJsaSubGraphIsomorphism
    extends ContainmentSolverWrapperJsaBase
{
    public ContainmentSolverWrapperJsaSubGraphIsomorphism() {
        super(QueryToGraph::match);
    }
}
