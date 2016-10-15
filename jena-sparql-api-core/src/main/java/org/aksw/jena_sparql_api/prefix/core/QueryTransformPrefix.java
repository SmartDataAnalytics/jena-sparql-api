package org.aksw.jena_sparql_api.prefix.core;

import org.aksw.jena_sparql_api.core.QueryTransform;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;

/**
 * Query transform to inject prefixes into a query
 * @author raven
 *
 */
public class QueryTransformPrefix
	implements QueryTransform
{
	private PrefixMapping prefixMapping;
	boolean doClone;

	public QueryTransformPrefix(PrefixMapping prefixMapping) {
		this(prefixMapping, true);
	}

	public QueryTransformPrefix(PrefixMapping prefixMapping, boolean doClone) {
		super();
		this.prefixMapping = prefixMapping;
		this.doClone = doClone;
	}

	@Override
	public Query apply(Query query) {
		Query result = doClone ? query.cloneQuery() : query;

		result.getPrefixMapping().setNsPrefixes(prefixMapping);
		return result;
	}
}
