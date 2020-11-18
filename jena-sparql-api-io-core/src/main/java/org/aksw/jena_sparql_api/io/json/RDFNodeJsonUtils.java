package org.aksw.jena_sparql_api.io.json;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Blank node preserving JSON serialization of RDFnode instances
 * i.e. of their underlying node and graph content.
 *
 * At present uses RDF/JSON but may be changed to JSON/LD once
 * I figure out how to preserve blank nodes with its writer machinery.
 *
 * Structure:
 * {
 *   node: strOf(x)
 *   graph: jsonOf(x.getModel())
 * }
 *
 * @author raven
 *
 */
public class RDFNodeJsonUtils {
    public static final String KEY_NODE = "node";
    public static final String KEY_GRAPH = "graph";

    // Jena does not seem to be very consistent in the use of its
    // blank node encoding facilities. Also, for most syntaxes the readers
    // writers to not allow for reuse of Node enocdings

    public static String nodeToStr(Node node) {
        String result = node.isBlank()
            ? "_:" + node.getBlankNodeLabel()
            : NodeFmtLib.str(node);

        return result;
    }

    public static Node strToNode(String str) {
        Node result = str.startsWith("_:")
            ? NodeFactory.createBlankNode(str.substring(2))
            : RiotLib.parse(str);

        return result;
    }

    public static JsonObject toJsonObject(Model model, Gson gson) {
        OutputStream baos = new ByteArrayOutputStream();
        RDFDataMgr.write(baos, model, RDFFormat.RDFJSON);
        String str = baos.toString();

        JsonObject result = gson.fromJson(str, JsonObject.class);
        return result;
    }



    public static JsonObject addJsonSerialization(Gson gson, JsonArray out, String name, Model model) {
        JsonObject result = null;
        if(!model.isEmpty()) {
            JsonObject data = toJsonObject(model, gson);

            result = new JsonObject();
            result.addProperty("name", name);
            result.add("data", data);
            out.add(result);
        }
        return result;
    }

    public static JsonObject toJsonObject(Dataset dataset, Gson gson) {
        JsonObject result = new JsonObject();
        JsonObject dgJson = toJsonObject(dataset.getDefaultModel(), gson);
        result.add("defaultGraph", dgJson);

        JsonArray arr = new JsonArray();
        Iterator<String> it = dataset.listNames();
        while(it.hasNext()) {
            String name = it.next();
            Model m = dataset.getNamedModel(name);
            addJsonSerialization(gson, arr, name, m);
        }

        if(arr.size() != 0) {
            result.add("namedGraphs", arr);
        }
        //JsonObject result = gson.fromJson(str, JsonObject.class);
        return result;
    }


    public static String toJsonNodeString(RDFNode n, Gson gson) {
        JsonObject jsonNodeLdOject = toJsonNodeObject(n, gson);
        String result = gson.toJson(jsonNodeLdOject);
        return result;
    }

    public static JsonObject toJsonNodeObject(RDFNode rdfNode, Gson gson) {
        Model model = rdfNode.getModel();
        Node n = rdfNode.asNode();

        String nodeStr = nodeToStr(n);
        JsonObject modelJson = toJsonObject(model, gson);

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
        Node node = strToNode(subjectStr);

        String graphStr = jsonNodeLd.get(KEY_GRAPH).toString();
        Model model = toModel(graphStr);

        RDFNode result = model.asRDFNode(node);

        return result;
    }

    public static Model toModel(String jsonString) {
        Model result = ModelFactory.createDefaultModel();
        RDFParserBuilder.create()
            .fromString(jsonString)
            .labelToNode(SyntaxLabels.createLabelToNodeAsGiven())
            .lang(Lang.RDFJSON)
            .parse(result);

        return result;
    }

    public static Model toModel(Model result, String jsonString) {
        RDFParserBuilder.create()
            .fromString(jsonString)
            .labelToNode(SyntaxLabels.createLabelToNodeAsGiven())
            .lang(Lang.RDFJSON)
            .parse(result);

        return result;
    }

    public static Dataset toDataset(String jsonString) {
        Dataset result = DatasetFactory.create();
        toDataset(result, jsonString);
        return result;
    }

    public static Dataset toDataset(Dataset result, String jsonString) {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(jsonString, JsonObject.class);
        JsonElement dge = root.get("defaultGraph");
        if(dge != null) {
            Model m = result.getDefaultModel();
            toModel(m, dge.toString());
        }

        JsonElement nge = root.get("namedGraphs");
        if(nge != null) {
            JsonArray arr = nge.getAsJsonArray();
            Iterator<JsonElement> it = arr.iterator();
            while(it.hasNext()) {
                JsonElement e = it.next();
                JsonObject obj = e.getAsJsonObject();

                String name = obj.get("name").getAsString();
                JsonObject data = obj.get("data").getAsJsonObject();

                Model m = result.getNamedModel(name);
                toModel(m, data.toString());
                //Model m = toModel(data.toString());
                //result.addNamedModel(name, m);
            }
        }

        return result;

//    	Dataset result = DatasetFactory.create();
//    	RDFParserBuilder.create()
//    		.fromString(jsonString)
//    		.labelToNode(SyntaxLabels.createLabelToNodeAsGiven())
//    		.lang(Lang.JSONLD)
//    		.parse(result);

    }

//	public static final  profile = new ParserProfileStd(RiotLib.factoryRDF(SyntaxLabels.createLabelToNodeRT()),
//  ErrorHandlerFactory.errorHandlerStd,
//  IRIResolver.create(),
//  PrefixMapFactory.createForInput(),
//  RIOT.getContext().copy(),
//  true, false);


//public static final ParserProfile profile = new ParserProfileStd(RiotLib.factoryRDF(SyntaxLabels.createLabelToNodeRT()),
//  ErrorHandlerFactory.errorHandlerStd,
//  IRIResolver.create(),
//  PrefixMapFactory.createForInput(),
//  RIOT.getContext().copy(),
//  true, false);
//SyntaxLabels.createLabelToNodeRT().get(null, x)

    // TODO Use as test
    public static void main(String[] args) {

        List<RDFNode> rdfNodes = Arrays.asList(
            ModelFactory.createDefaultModel().createResource().addProperty(RDF.type, OWL.Class),
            ModelFactory.createDefaultModel().createResource(RDFS.Resource.getURI()).addProperty(RDF.type, OWL.Class)
        );

        for(RDFNode r : rdfNodes) {
            String str = RDFNodeJsonUtils.toJsonNodeString(r, new Gson());

            System.out.println(str);
            RDFNode rdfNode = RDFNodeJsonUtils.toRDFNode(str, new Gson());
              RDFDataMgr.write(System.out, rdfNode.getModel(), RDFFormat.TURTLE_PRETTY);
        }

    }
}
