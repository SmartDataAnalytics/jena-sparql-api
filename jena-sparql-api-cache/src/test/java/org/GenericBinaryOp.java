package org;

import java.util.function.BiFunction;

public interface GenericBinaryOp<O>
    extends BiFunction<Object, Object, O>
{
}