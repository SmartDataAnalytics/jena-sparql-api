package org.aksw.jena_sparql_api.utils.model;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Utils to de-/serialize an RDFNode 'x' together with its graph as JSON object
 * using on JSON-LD of the folowing structure
 * 
 * {
 *   node: strOf(x)
 *   graph: jsonLdOf(x.getModel())
 * }
 * 
 * @author raven
 *
 */
public class RDFNodeJsonLdUtils {
	public static final String KEY_NODE = "node";
	public static final String KEY_GRAPH = "graph";
	
	public static JsonObject toJsonLdObject(Model model, Gson gson) {
        OutputStream baos = new ByteArrayOutputStream();
        RDFDataMgr.write(baos, model, RDFFormat.JSONLD);
        String str = baos.toString();
        
        JsonObject result = gson.fromJson(str, JsonObject.class);
        return result;
	}

	public static String toJsonNodeLdString(RDFNode n, Gson gson) {
		JsonObject jsonNodeLdOject = toJsonNodeLdObject(n, gson);
		String result = gson.toJson(jsonNodeLdOject);
		return result;
	}

    public static JsonObject toJsonNodeLdObject(RDFNode n, Gson gson) {
    	Model model = n.getModel();

    	String nodeStr = NodeFmtLib.str(n.asNode());
    	JsonObject modelJson = toJsonLdObject(model, gson);    	

        JsonObject result = new JsonObject();
        result.addProperty(KEY_NODE, nodeStr);
        result.add(KEY_GRAPH, modelJson);

        return result;
    }
    
    public static RDFNode toRDFNode(String jsonNodeLdString, Gson gson) {
    	JsonObject jsonNodeLd = gson.fromJson(jsonNodeLdString, JsonObject.class);
    	RDFNode result = toRDFNode(jsonNodeLd);
    	return result;
    }
    
    public static RDFNode toRDFNode(JsonObject jsonNodeLd) {
    	String subjectStr = jsonNodeLd.get(KEY_NODE).getAsString();
    	Node node = RiotLib.parse(subjectStr);
    	
    	String graphStr = jsonNodeLd.get(KEY_GRAPH).toString();
    	Model model = toModel(graphStr);
    	
    	RDFNode result = model.asRDFNode(node);
    	
    	return result;
    }
    
    public static Model toModel(String jsonLdString) {
    	Model result = ModelFactory.createDefaultModel();
    	RDFDataMgr.read(result, new ByteArrayInputStream(jsonLdString.getBytes()), Lang.JSONLD);
    	return result;
    }
    
    // TODO Use as test
    public static void main(String[] args) {

    	List<RDFNode> rdfNodes = Arrays.asList(     			
    		ModelFactory.createDefaultModel().createResource().addProperty(RDF.type, OWL.Class),
    		ModelFactory.createDefaultModel().createResource(RDFS.Resource.getURI()).addProperty(RDF.type, OWL.Class)
    	);
    	
    	for(RDFNode r : rdfNodes) {
	        String str = RDFNodeJsonLdUtils.toJsonNodeLdString(r, new Gson());
	
	        System.out.println(str);
	        RDFNode rdfNode = RDFNodeJsonLdUtils.toRDFNode(str, new Gson());
  	        RDFDataMgr.write(System.out, rdfNode.getModel(), RDFFormat.TURTLE_PRETTY);
    	}

    }
}
