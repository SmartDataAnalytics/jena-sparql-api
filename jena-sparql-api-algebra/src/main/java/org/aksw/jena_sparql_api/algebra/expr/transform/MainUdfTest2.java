package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.core.utils.RDFDataMgrEx;
import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctions;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class MainUdfTest2 {
	public static void main(String[] args) {
		Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
		Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/jena"));
		RDFDataMgrEx.execSparql(model, "udf-inferences.sparql");

		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

		UserDefinedFunctions.load(model, profiles);

		
//		System.out.println(model.size());
	}
}
