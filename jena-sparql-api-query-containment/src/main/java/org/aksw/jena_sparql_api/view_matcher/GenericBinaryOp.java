package org.aksw.jena_sparql_api.view_matcher;

import java.util.function.BiFunction;

public interface GenericBinaryOp<O>
    extends BiFunction<Object, Object, O>
{
}