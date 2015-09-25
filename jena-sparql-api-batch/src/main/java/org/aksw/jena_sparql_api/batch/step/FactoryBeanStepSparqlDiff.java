package org.aksw.jena_sparql_api.batch.step;

import java.util.Map.Entry;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.batch.ListServiceResourceShape;
import org.aksw.jena_sparql_api.batch.processor.ItemProcessorModifierDatasetGraphDiff;
import org.aksw.jena_sparql_api.batch.reader.ItemReaderDatasetGraph;
import org.aksw.jena_sparql_api.batch.writer.ItemWriterSparqlDiff;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.modifier.Modifier;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.FactoryBean;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class FactoryBeanStepSparqlDiff
	implements FactoryBean<Step>
{

	protected StepBuilder stepBuilder;
	protected ResourceShape shape;

	protected String name;
	//protected TODO How to represent the shape?
	protected int chunkSize;
	protected QueryExecutionFactory sourceQef;
	protected Concept concept;
	//protected ListService<>
	protected Modifier<? super DatasetGraph> modifier;


	protected UpdateExecutionFactory targetUef;

	public FactoryBeanStepSparqlDiff chunk(int chunkSize) {
		this.chunkSize = chunkSize;
		return this;
	}

	public FactoryBeanStepSparqlDiff shape(ResourceShape shape) {
		this.shape = shape;
		return this;
	}


	public FactoryBeanStepSparqlDiff modifier(Modifier<? super DatasetGraph> modifier) {
		this.modifier = modifier;
		return this;
	}

	public FactoryBeanStepSparqlDiff modifier(Concept concept) {
		this.concept = concept;
		return this;
	}

	public FactoryBeanStepSparqlDiff setName(String name) {
		this.name = name;
		return this;
	}

	public FactoryBeanStepSparqlDiff setSource(QueryExecutionFactory sourceQef) {
		this.sourceQef = sourceQef;
		return this;
	}

	public FactoryBeanStepSparqlDiff setConcept(Concept concept) {
		this.concept = concept;
		return this;
	}


	public FactoryBeanStepSparqlDiff setTarget(UpdateExecutionFactory targetUef) {
		this.targetUef = targetUef;
		return this;
	}


	@Override
	public Step getObject() throws Exception {
		ListService<Concept, Node, DatasetGraph> listService = new ListServiceResourceShape(sourceQef, shape);
		ItemReader<Entry<Node, DatasetGraph>> itemReader = new ItemReaderDatasetGraph(listService, concept);
		ItemProcessor<Entry<? extends Node, ? extends DatasetGraph>, Entry<Node, Diff<DatasetGraph>>> itemProcessor = new ItemProcessorModifierDatasetGraphDiff(modifier);
		ItemWriter<Entry<? extends Node, ? extends Diff<? extends DatasetGraph>>> itemWriter = new ItemWriterSparqlDiff(targetUef);

	    Step result = stepBuilder
	    		.<Entry<Node, DatasetGraph>, Entry<Node, Diff<DatasetGraph>>>chunk(chunkSize)
	            .reader(itemReader)
	            .processor(itemProcessor)
	            .writer(itemWriter)
	            .build();

	    return result;
	}

	@Override
	public Class<?> getObjectType() {
		return Step.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
