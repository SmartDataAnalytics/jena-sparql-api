rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlStep;
  if(e) {
    var result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepChunk',
      name: e.name + '-slave',
      chunkSize: e.chunk,
      itemReader: {
        type: 'org.aksw.jena_sparql_api.batch.reader.ItemReaderQuad',
        scope: 'step',
        'currentItemCount': '#{ stepExecutionContext[minValue] }',
        'maxItemCount': '#{ stepExecutionContext[maxValue] }',
        'pageSize': e.readSize || e.chunk,
        'qef': e.source,
        'query': e.query,
        'modifiers': e.modifiers,
        'hop': e.hop
      },
      itemProcessor: {
          type: 'org.springframework.batch.item.validator.ValidatingItemProcessor',
          scope: 'step',
          filter: true,
          validator: {
            type: 'org.aksw.jena_sparql_api.batch.step.ValidatorQuadByPredicate',
            ctor: [{
              type: 'org.aksw.jena_sparql_api.batch.reader.PredicateQuadExpr',
              ctor: ['<http://jsa.aksw.org/fn/term/valid>(?g) && <http://jsa.aksw.org/fn/term/valid>(?s) && <http://jsa.aksw.org/fn/term/valid>(?p) && <http://jsa.aksw.org/fn/term/valid>(?o)']
            }]
          }
      },
      itemWriter: {
          type: 'org.aksw.jena_sparql_api.batch.writer.ItemWriterQuad',
          scope: 'step',
          target: e.target,
          isDelete: e.isDelete
      }
    };

    // Add the partitioner
    result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepPartitioner',
      name: e.name + '-master',
      throttle: e.throttle,
      taskExecutor: e.taskExecutor,
      partitioner: {
        type: 'org.aksw.jena_sparql_api.batch.step.PartitionerSparqlSlice',
        source: e.source,
        query: e.query
      },
      slaveStep: result
    };

    //_(result).extend(e);
    //result['type'] = 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlPipe';
  }
  return result;
});
//
//
//    public Step createInstance() throws Exception {
//        Modifier<DatasetGraph> modifier = ModifierList.<DatasetGraph>create(modifiers);
//
//
//        ListService<Concept, Node, DatasetGraph> listService = createListService();
//        //ItemReader<Entry<Node, DatasetGraph>> itemReader = new ItemReaderDatasetGraph(listService, concept);
//        ItemReaderDatasetGraph itemReader = new ItemReaderDatasetGraph(listService, concept);
//        ItemProcessor<Entry<? extends Node, ? extends DatasetGraph>, Entry<Node, Diff<DatasetGraph>>> itemProcessor = new ItemProcessorModifierDatasetGraphDiff(modifier);
//        ItemWriter<Entry<? extends Node, ? extends Diff<? extends DatasetGraph>>> itemWriter = new ItemWriterSparqlDiff(targetUef);
//
//        itemReader.setPageSize(chunkSize);
//
//        //StepBuilderFactory stepBuilders = batchConfig.stepBuilders();
//        StepBuilder stepBuilder = stepBuilders.get(name);
//
//        Step result = stepBuilder
//                .<Entry<Node, DatasetGraph>, Entry<Node, Diff<DatasetGraph>>>chunk(chunkSize)
//                .reader(itemReader)
//                .processor(itemProcessor)
//                .writer(itemWriter)
//                .build();
//
//        return result;
//    }
//
//    @Override
//    public Class<?> getObjectType() {
//        return Step.class;
//    }
//}