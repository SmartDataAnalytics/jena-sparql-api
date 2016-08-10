package org.aksw.jena_sparql_api.concept_cache.op;

import org.aksw.commons.collections.trees.TreeOps;
import org.aksw.commons.collections.trees.TreeOps;
import org.apache.jena.sparql.algebra.Op;

public class OpTreeOps {
	public static TreeOps<Op> instance;
	
	public static TreeOps<Op> get() {
		if(instance == null) {
			instance = new TreeOps<>(
					OpUtils::getSubOps,
					null,
					OpUtils::copy);
		}

		return instance;
	}
}
