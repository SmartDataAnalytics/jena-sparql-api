package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MvnEntity;
import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobBinding;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jena_sparql_api.conjure.traversal.engine.FunctionAssembler;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.aksw.jena_sparql_api.http.repository.impl.ResourceStoreImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

interface ConjureCacheEntry {
	RdfDataPod getDataPod();
	Model getMetadata();
	
	
	
	// Model getDcatMetadata();

	// 
}



public class ExecutionUtils {
	private static final Logger logger = LoggerFactory.getLogger(ExecutionUtils.class);

	/**
	 * Create an Id from a given resource with the following precedence of preferences:
	 * - maven identifier
	 * - URI
	 * - generic hash
	 * 
	 * @param r
	 * @return
	 */
	public static String deriveId(Resource r) {
		MvnEntity ds = ModelFactory.createDefaultModel().createResource().as(MvnEntity.class);
		String mvnId = Arrays.asList(ds.getGroupId(), ds.getArtifactId(), ds.getVersion(), ds.getClassifier()).stream()
			.filter(Objects::nonNull)
			.collect(Collectors.joining(":"));
		
		String result = !mvnId.isEmpty()
				? mvnId
				: r.isURIResource()
					? r.getURI()
					: ResourceTreeUtils.createGenericHash(r);

		return result;
	}

	/**
	 * Execute a job and return a dcat model of the result
	 * 
	 * @param job
	 * @param taskContexts
	 * @param repo
	 * @param cacheStore
	 * @return
	 */
	public static List<DcatDataset> executeJob(
			Job job,
			List<TaskContext> taskContexts,
			HttpResourceRepositoryFromFileSystem repo,
			ResourceStore cacheStore) {
		
		List<DcatDataset> result = new ArrayList<>();
		Model resultModel = ModelFactory.createDefaultModel();
		
		Op jobOp = job.getOp();
	    Op semanticJobOp = OpUtils.stripCache(jobOp);
	    String jobId = ResourceTreeUtils.createGenericHash(semanticJobOp);

		
		for(TaskContext taskContext : taskContexts) {

			Resource inputRecord = taskContext.getInputRecord();

			// Try to create a hash from the input record
			String inputRecordId = deriveId(inputRecord);

			String completeId = inputRecordId + "/" + jobId;

			logger.info("Processing: " + inputRecord + " with complete id " + completeId);

			
			// For the ID, there are these artifacts:
			// ID/data - the actual data
			// ID/dcat - dcat metadata
			// ID/exectx - execution context

//			RdfHttpEntityFile dataEntity = ResourceStoreImpl.getOrCacheEntity(repo, store, uri, serializer, contentSupplier);
//			if(dataEntity == null) {
//				Entry<Model, RdfDataPod> e = executeJob(job, repo, taskContext, inputRecord);
//	
//			}
			try {
	
				Entry<RdfHttpEntityFile, Model> dataEntry = ResourceStoreImpl.requestModel(repo, cacheStore, completeId + "/data", RDFFormat.TURTLE_PRETTY,		
						() -> {
							RdfDataPod tmp = executeJob(job, repo, taskContext, inputRecord);							
							Model r = tmp.getModel();
							return r;
						});
	
				Model provModel = ResourceStoreImpl.requestModel(repo, cacheStore, completeId + "/dcat", RDFFormat.TURTLE_PRETTY,		
						() -> {
							Model r = createProvenanceData(job, inputRecord).getModel();
							//RdfDataPod tmp = executeJob(job, repo, taskContext, inputRecord);							
							return r;
						}).getValue();
	
				resultModel.add(provModel);
				DcatDataset dcatDataset = provModel.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList()
						.get(0)
						.inModel(resultModel)
						.as(DcatDataset.class);
				result.add(dcatDataset);
				
				Collection<DcatDistribution> dists = dcatDataset.getDistributions(DcatDistribution.class);
				DcatDistribution dist = resultModel.createResource().as(DcatDistribution.class);
				dists.add(dist);
				dist.setDownloadURL(dataEntry.getKey().getAbsolutePath().toUri().toString());
	
//				System.out.println("BEGIN OUTPUT");
//				RDFDataMgr.write(System.out, dcatDataset.getModel(), RDFFormat.TURTLE_PRETTY);
//				System.out.println("END OUTPUT");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return result;
	}

	/**
	 * TODO The result of this computation are two datasets:
	 * The result dataset and its dcat record dataset. How to chache that?
	 * - Cache both - if so: What to use as the id of the metadata? Maybe $classifier$ + -meta?
	 * - Cache only the result dataset
	 * - 
	 * 
	 * 
	 * 
	 * 
	 * @param job
	 * @param repo
	 * @param taskContext
	 * @param inputRecord
	 * @return
	 */
	private static RdfDataPod executeJob(Job job, HttpResourceRepositoryFromFileSystem repo, TaskContext taskContext,
			Resource inputRecord) {
		RDFNode jobContext = ModelFactory.createDefaultModel().createResource();

		Set<String> mentionedVars = OpUtils.mentionedVarNames(job.getOp());
		logger.debug("Mentioned vars: " + mentionedVars);
		
		Map<String, DataRef> dataRefMapping = taskContext.getDataRefMapping();
		// Get the subset of mentioned vars for which no entry in the task context
		// exists
		// If there is just a single dataref and one unbound var
		// auto-bind them to the input calatog record resource
		Set<String> unmatchedVars = new HashSet<>(mentionedVars);
		unmatchedVars.removeAll(taskContext.getDataRefMapping().keySet());
		
		if(unmatchedVars.size() > 1) {
			throw new RuntimeException("Too many unmatched vars: " + unmatchedVars);
		} else if(unmatchedVars.size() == 1) {
			String unmatchedVarName = unmatchedVars.iterator().next();
			if(dataRefMapping.size() == 1) {
				DataRef entry = dataRefMapping.values().iterator().next();
				dataRefMapping.put(unmatchedVarName, entry);
				logger.info("Autobind of " + unmatchedVarName + " to " + entry);
			} else {
				throw new RuntimeException("Could not auto-bind var " + unmatchedVarName);
			}
		}	

		OpExecutorDefault executor = new OpExecutorDefault(repo, taskContext);

		// Create a copy of the workflow spec and substitute the variables
		//Map<String, Op> map = Collections.emptyMap();//Collections.singletonMap("dataRef", OpDataRefResource.from(model, DataRefUrl.create(model, url)));			
		//Op effectiveWorkflow = OpUtils.copyWithSubstitution(job.getOp(), map::get);

		Op effectiveWorkflow = job.getOp();
		
		// Add an initial empty binding
		Multimap<Var, Node> valueMap = LinkedHashMultimap.create();
		
		FunctionAssembler assembler = new FunctionAssembler();
		for(JobBinding bspec : job.getJobBindings()) {
			String varName = bspec.getVarName();
			Var var = Var.alloc(varName);
			OpTraversal trav = bspec.getTraversal();
			
			Function<RDFNode, Set<RDFNode>> fn = trav.accept(assembler);
			
			Set<RDFNode> values = fn.apply(jobContext);
			for(RDFNode value : values) {
				Node node = value.asNode();
				valueMap.put(var, node);
			}
		}

		// Create the set of bindings
		// TODO Is there a nifty way to create the cartesian product with flatMap?
		List<Binding> currentBindings = new ArrayList<>();
		currentBindings.add(BindingFactory.root());

		List<Binding> nextBindings = new ArrayList<>();
		for(Entry<Var, Collection<Node>> e : valueMap.asMap().entrySet()) {
			Var k = e.getKey();
			Collection<Node> vs = e.getValue();
			
			for(Node node : vs) {
				for(Binding cb : currentBindings) {
					Binding nb = BindingFactory.binding(cb, k, node);
					nextBindings.add(nb);
				}
			}
			
			List<Binding> xtmp = currentBindings;
			currentBindings = nextBindings;
			nextBindings = xtmp;
			nextBindings.clear();
		}

		if(currentBindings.isEmpty() || currentBindings.size() > 1) {
			throw new RuntimeException("Can only handle exactly a single binding at present");
		}
		
		Binding binding = currentBindings.iterator().next();
		
		System.out.println("BINDING: " + binding);
		

		// Set up a dataset processing expression		
		logger.info("Conjure spec is:");
		RDFDataMgr.write(System.err, effectiveWorkflow.getModel(), RDFFormat.TURTLE_PRETTY);
		
		RdfDataPod resultDataPod = effectiveWorkflow.accept(executor);
//		try() {
//			Model rmodel = resultDataPod.getModel();
////			try(RDFConnection conn = resultDataPod.openConnection()) {
////				// Print out the data that is the process result
////				Model rmodel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
////				
//				RDFDataMgr.write(System.out, rmodel, RDFFormat.TURTLE_PRETTY);
////			}
//		} catch(Exception e) {
//			logger.warn("Failed to process " + taskContext, e);
//		}
		
		return resultDataPod;
		//return Maps.immutableEntry(resultDcat.getModel(), resultDataPod);


//		Resource resultDcat = createProvenanceData(job, inputRecord);
//		
//		
//		return Maps.immutableEntry(resultDcat.getModel(), resultDataPod);
		
		//RDFDataMgr.write(System.out, resultDcat.getModel(), RDFFormat.TURTLE_PRETTY);
		// TODO Create the output DCAT record:
		// d' wasDerivedFrom d
		// d' wasGeneratedBy activity
		// activity prov:used d ; startedAtTime ; endedAtTime
//			   prov:qualifiedUsage [
//			                        a prov:Usage;
//			                        prov:entity  :process;
//			                        prov:hadRole :processSpec;          
//			                     ];
		// 
		// 
	}

	private static Resource createProvenanceData(Job job, Resource inputRecord) {
		Model resultModel = ModelFactory.createDefaultModel();
		Resource inputRecordX = inputRecord.inModel(resultModel.add(inputRecord.getModel()));
		
		// TODO We only need to output the job model once if it was the same for every task
		Resource jobX = job.inModel(resultModel.add(job.getModel()));
		
		Resource resultDcat = resultModel.createResource()
			.addProperty(RDF.type, prov("Entity"))
			.addProperty(RDF.type, DCAT.Dataset);

		// Copy the input record
		Resource association = resultModel.createResource()
				.addProperty(RDF.type, prov("Association"))
				.addProperty(prov("hadPlan"), job);

		Resource activity = resultModel.createResource()
				.addProperty(RDF.type, prov("Activity"))
				.addProperty(prov("qualifiedAssociation"), association)
				.addProperty(prov("used"), inputRecord) // TODO Ensure the input record is an entity
				.addProperty(prov("used"), job);

		resultDcat
			.addProperty(prov("wasGeneratedBy"), activity);
		return resultDcat;
	}
	
	public static Property prov(String name) {
		return ResourceFactory.createProperty("http://www.w3.org/ns/prov#" + name);
	}

}
