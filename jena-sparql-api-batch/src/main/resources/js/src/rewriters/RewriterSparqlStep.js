rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlStep;
  if(e) {
    result = {
      type: '',
      reader: {
        type: 'org.aksw.jena_sparql_api.batch.reader.ItemReaderDatasetGraph',
        scope: 'step'
      },
      processor: {
      },
      writer: {
        type: 'org.aksw.jena_sparql_api.batch.writer.ItemWriterSparqlDiff',
        uef: e.target
      }

    };
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