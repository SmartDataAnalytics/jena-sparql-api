package org.aksw.jena_sparql_api.sparql_path.utils;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;

public class VarUtils {

	/**
	 * Returns a list of variable names as strings for a given iterable of Var objects.
	 * 
	 * @param vars
	 * @return
	 */
	public static List<String> getVarNames(Iterable<Var> vars) {
		List<String> result = new ArrayList<String>();
		
		for(Var var : vars) {
			result.add(var.getName());
		}
		
		return result;
	}
}