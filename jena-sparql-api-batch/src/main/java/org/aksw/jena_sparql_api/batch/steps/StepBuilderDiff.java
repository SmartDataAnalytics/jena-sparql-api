package org.aksw.jena_sparql_api.batch.steps;

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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class StepBuilderDiff {

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

	public StepBuilderDiff chunk(int chunkSize) {
		this.chunkSize = chunkSize;
		return this;
	}

	public StepBuilderDiff shape(ResourceShape shape) {
		this.shape = shape;
		return this;
	}

	public StepBuilderDiff modifier(Modifier<? super DatasetGraph> modifier) {
		this.modifier = modifier;
		return this;
	}

	public StepBuilderDiff modifier(Concept concept) {
		this.concept = concept;
		return this;
	}

	public StepBuilderDiff setName(String name) {
		this.name = name;
		return this;
	}

	public StepBuilderDiff sourceQef(QueryExecutionFactory sourceQef) {
		this.sourceQef = sourceQef;
		return this;
	}

	public StepBuilderDiff concept(Concept concept) {
		this.concept = concept;
		return this;
	}


	public StepBuilderDiff targetUef(UpdateExecutionFactory targetUef) {
		this.targetUef = targetUef;
		return this;
	}

	public Step build() {
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


}
