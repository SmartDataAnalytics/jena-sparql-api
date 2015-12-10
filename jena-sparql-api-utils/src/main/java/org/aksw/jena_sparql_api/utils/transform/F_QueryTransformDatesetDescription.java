package org.aksw.jena_sparql_api.utils.transform;

import com.google.common.base.Function;
import com.hp.hpl.jena.query.Query;

public class F_QueryTransformDatesetDescription
	implements Function<Query, Query>
{
	@Override
	public Query apply(Query query) {
		Query result = ElementTransformDatasetDescription.rewrite(query);
		return result;
	}

	public static final F_QueryTransformDatesetDescription fn = new F_QueryTransformDatesetDescription();
}
