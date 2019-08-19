package org.aksw.jena_sparql_api.user_defined_function;

import java.io.ByteArrayInputStream;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;

public class UdfTest {
	public static void main(String[] args) {
		JenaPluginUtils.scan(UdfTest.class);
		
		String tmp = "<http://www.example.org/myfn>\n" + 
				"  <http://ns.aksw.org/jena/udf/simpleDefinition> (\"<http://jena.apache.org/ARQ/function#bnode>(?x)\" \"x\") ;\n" + 
				"  .";
		
		Model m = ModelFactory.createDefaultModel(); 
		RDFDataMgr.read(m, new ByteArrayInputStream(tmp.getBytes()), Lang.TURTLE);
		
		UserDefinedFunctionResource d = m.createResource("http://www.example.org/myfn").as(UserDefinedFunctionResource.class);
//		//UserDefinedFunctionDefinition udfd = d.toJena();
//		System.out.println(udfd.getUri());
//		System.out.println(udfd.getBaseExpr());
//		System.out.println(udfd.getArgList());
//		System.out.println(d.getDefinitions().stream().filter(x -> x.mapsToPropertyFunction()).findAny());
	}

}
