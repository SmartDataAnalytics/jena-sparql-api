package org.aksw.jena_sparql_api.batch.to_review;

import java.util.Map;

import com.google.common.base.Function;

public interface MapTransformer
	extends Function<Map<String, Object>, Map<String, Object>>
{
}