package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Map.Entry;

import com.codepoetics.protonpack.functions.TriFunction;

public interface NodeMapper<A, B, M, C, V>
//	extends TriFunction<A, B, TreeMapping<A, B, M, V>, Entry<C, V>>
{
	Entry<C, V> apply(A a, B b, TreeMapping<A, B, M, V> treeMapping);
}
