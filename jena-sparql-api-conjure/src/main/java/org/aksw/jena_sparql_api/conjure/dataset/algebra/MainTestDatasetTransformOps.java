package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;

public class MainTestDatasetTransformOps {
	public static void main(String[] args) {
		JenaSystem.init();
		
		JenaPluginUtils.scan(Op.class);
		
		Model m = ModelFactory.createDefaultModel();
		OpDataRefResource a = m.createResource().as(OpDataRefResource.class)
				;//.setDatasetId("myDatasetId");
		
		OpUpdateRequest b = m.createResource()
				.as(OpUpdateRequest.class)
				.addUpdateRequest("DELETE { ?s a ?o } WHERE { ?s a ?o }")
				.setSubOp(a);
		
		
		RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
	
		System.out.println(b.getSubOp() instanceof OpDataRefResource); 
		
		
		OpVisitor<Op> visitor = new OpMapper();
		
		Op result = b.accept(visitor);
		System.out.println("Result: " + result);
		
	}
}
