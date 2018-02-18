package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.query_containment.core.SparqlQueryContainmentUtils;
import org.apache.jena.query.Query;

public class ContainmentSolverWrapperJsaSubgraphIsomorphismIndex
	extends ContainmentSolverWrapperBase
{
	@Override
	public boolean entailed(Query q1, Query q2) {
        boolean result = SparqlQueryContainmentUtils.tryMatch(q2, q1);

        return result;
	}

}
