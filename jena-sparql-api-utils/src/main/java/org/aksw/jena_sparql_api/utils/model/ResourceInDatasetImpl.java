package org.aksw.jena_sparql_api.utils.model;

import org.aksw.jena_sparql_api.utils.DatasetUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDFS;

/**
 * A specific resource in the default graph or one of the named graphs of a dataset. 
 * 
 * Note, that resources obtained via .as(viewClass) will retain the reference to the dataset's
 * model, but the one to the dataset itself will be lost
 * 
 * @author raven
 *
 */
public class ResourceInDatasetImpl
	extends ResourceImpl
	implements ResourceInDataset
{
	protected Dataset dataset;
	protected String graphName;
	protected Node graphNameNode;
	
	public static void main(String[] args) {
		// Experiments for analyzing equals behavior on datasets and models
		Dataset ds = DatasetFactory.create();
		
		// False:
		System.out.println("Reference equality of result of multiple .getDefaultModel() invocations: " + (ds.getDefaultModel() == ds.getDefaultModel()));

		// True:
		System.out.println("State equality of result of multiple .getDefaultModel() invocations: " + (ds.getDefaultModel().equals(ds.getDefaultModel())));


		Resource r1 = ds.getDefaultModel().createResource();
		Resource r2 = ds.getDefaultModel().createResource("urn:x-test:foo");
		
		// False:
		System.out.println("Reference equality models of the same graph: " + (r1.getModel() == r2.getModel()));

		// True:
		System.out.println("State equality models of the same graph: " + (r1.getModel().equals(r2.getModel())));
		
		// Even after insert?
		
		r1.addProperty(RDFS.label, "r1");
		r2.addProperty(RDFS.label, "r2");
		
		// True:
		System.out.println("Counts should be equal: " + r1.getModel().size() + " " + r2.getModel().size());
		
		
		ResourceInDataset x1 = ResourceInDatasetImpl.createFromCopyIntoDefaultGraph(r1);
		RDFDataMgr.write(System.out, x1.getDataset(), RDFFormat.TRIG_PRETTY);

		ResourceInDataset x2 = ResourceInDatasetImpl.createFromCopyIntoResourceGraph(r2);
		RDFDataMgr.write(System.out, x2.getDataset(), RDFFormat.TRIG_PRETTY);

	}
	
	public static ResourceInDataset renameResource(ResourceInDataset old, String uri) {
		Dataset dataset = old.getDataset();
		String graphName = old.getGraphName();
		Resource n = ResourceUtils.renameResource(old, uri);
		Node newNode = n.asNode();
		ResourceInDataset result = new ResourceInDatasetImpl(dataset, graphName, newNode);
		return result;
	}
	
	public static ResourceInDataset createAnonInDefaultGraph() {
		Dataset ds = DatasetFactory.create();
		Node root = NodeFactory.createBlankNode();
		
		ResourceInDataset result = new ResourceInDatasetImpl(ds, Quad.defaultGraphIRI.getURI(), root);
		return result;
	}
	
	/**
	 * Copy the content of a given resource's model into this resource's dataset
	 * and return an ResourceInDataset instance for it.
	 * 
	 * @param r
	 */
	public static ResourceInDataset createFromCopyIntoDefaultGraph(Resource r) {
		ResourceInDataset result = createFromCopy(
				DatasetFactory.create(),
				Quad.defaultGraphIRI.getURI(),
				r);
		
		return result;	
	}

	public static ResourceInDataset createInDefaultGraph(Node node) {
		Dataset dataset = DatasetFactory.create();
		ResourceInDataset result = new ResourceInDatasetImpl(dataset, Quad.defaultGraphIRI.getURI(), node);
		return result;	
	}

	public static ResourceInDataset createFromCopyIntoResourceGraph(Resource r) {
		ResourceInDataset result = createFromCopy(
				DatasetFactory.create(),
				r.getURI(),
				r);
		
		return result;	
	}

	public static ResourceInDataset createFromCopy(Dataset tgt, String graphName, Resource src) {
		Model m = DatasetUtils.getDefaultOrNamedModel(tgt, graphName);
		GraphUtil.addInto(m.getGraph(), src.getModel().getGraph());
		
		Node root = src.asNode();
		ResourceInDataset result = new ResourceInDatasetImpl(tgt, Quad.defaultGraphIRI.getURI(), root);
		
		return result;	
	}

	
	public ResourceInDatasetImpl(Dataset dataset, String graphName, Node node) {
		super(node, (EnhGraph)DatasetUtils.getDefaultOrNamedModel(dataset, graphName));
		dataset.getNamedModel(graphName);
		this.dataset = dataset;
		this.graphName = graphName;
	}
	
	@Override
	public String getGraphName() {
		return graphName;
	}

	@Override
	public Dataset getDataset() {
		return dataset;
	}

	@Override
	public ResourceInDataset inDataset(Dataset other) {
		return new ResourceInDatasetImpl(other, graphName, graphNameNode);
	}
}