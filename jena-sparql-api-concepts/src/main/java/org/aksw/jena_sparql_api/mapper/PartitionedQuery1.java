package org.aksw.jena_sparql_api.mapper;

import java.util.Collections;
import java.util.List;

import org.apache.jena.sparql.core.Var;

/**
 * Special case of a query partitioned by a single variable
 *
 * @author raven
 *
 */
public interface PartitionedQuery1
    extends PartitionedQuery
{
    Var getPartitionVar();

    @Override
    default List<Var> getPartitionVars() {
        Var partitionVar = getPartitionVar();
        return Collections.singletonList(partitionVar);
    }
}

