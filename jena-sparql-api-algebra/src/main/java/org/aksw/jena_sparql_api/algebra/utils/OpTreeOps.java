package org.aksw.jena_sparql_api.algebra.utils;

import org.aksw.commons.collections.trees.TreeOps2;
import org.aksw.commons.collections.trees.TreeOps2;
import org.apache.jena.sparql.algebra.Op;

public class OpTreeOps {
	public static TreeOps2<Op> instance;
	
	public static TreeOps2<Op> get() {
		if(instance == null) {
			instance = new TreeOps2<>(
					OpUtils::getSubOps,
					null,
					OpUtils::copy);
		}

		return instance;
	}
}
