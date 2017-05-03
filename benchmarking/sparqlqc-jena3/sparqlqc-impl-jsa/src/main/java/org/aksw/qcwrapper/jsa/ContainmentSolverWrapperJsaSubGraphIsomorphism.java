package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.jgrapht.transform.QueryToGraph;

public class ContainmentSolverWrapperJsaSubGraphIsomorphism
    extends ContainmentSolverWrapperJsaBase
{
    public ContainmentSolverWrapperJsaSubGraphIsomorphism() {
        super(QueryToGraph::match);
    }
}
