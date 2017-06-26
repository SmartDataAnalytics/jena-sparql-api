package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.view_matcher.QueryToGraphMatcher;

public class ContainmentSolverWrapperJsaSubGraphIsomorphism
    extends ContainmentSolverWrapperJsaBase
{
    public ContainmentSolverWrapperJsaSubGraphIsomorphism() {
        super(QueryToGraphMatcher::match);
    }
}
