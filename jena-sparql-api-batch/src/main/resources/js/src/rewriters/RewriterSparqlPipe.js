rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlPipe;
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
        'query': e.query
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
        query: e.query,
        pageSize: e.chunk
      },
      slaveStep: result
    };

    //_(result).extend(e);
    //result['type'] = 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlPipe';
  }
  return result;
});


//        ItemProcessor<? super Quad, ? extends Quad> processor;
//        if(predicate != null) {
//            ValidatingItemProcessor<Quad> validatingProcessor = new ValidatingItemProcessor<Quad>();
//            validatingProcessor.setValidator(new ValidatorQuadByPredicate(predicate));
//            validatingProcessor.setFilter(true);
//            try {
//                validatingProcessor.afterPropertiesSet();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            processor = validatingProcessor;
//        } else {
//            processor = new PassThroughItemProcessor<Quad>();
//        }
//
        //ItemProcessor<? super Quad, ? extends Quad> processor = new PassThroughItemProcessor<Quad>();
        //ItemWriterQuad writer = new ItemWriterQuad(target, isDelete);

        //reader.setPageSize(chunkSize);


        //SimplePartitioner x

//        Step result = stepBuilder
//                .<Quad, Quad>chunk(chunkSize)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                //.throttleLimit(throttle)
//                .build()
//                ;




//        if(throttle != null) {
//            Step slaveStep = result;
//
//            Partitioner partitioner = new PartitionerSparqlSlice(source, query);
//            result = stepBuilder
//                .partitioner(slaveStep)
//                .partitioner(name, partitioner)
//                .taskExecutor(taskExecutor)
//                .gridSize(throttle)
//                .build()
//                ;
//        }
