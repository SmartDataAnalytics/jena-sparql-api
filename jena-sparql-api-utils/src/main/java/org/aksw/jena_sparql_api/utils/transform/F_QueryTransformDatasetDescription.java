package org.aksw.jena_sparql_api.utils.transform;

import java.util.function.Function;

import org.apache.jena.query.Query;

public class F_QueryTransformDatasetDescription
    implements Function<Query, Query>
{
    @Override
    public Query apply(Query query) {
        Query result = ElementTransformDatasetDescription.rewrite(query);
        return result;
    }

    public static final F_QueryTransformDatasetDescription fn = new F_QueryTransformDatasetDescription();
}
