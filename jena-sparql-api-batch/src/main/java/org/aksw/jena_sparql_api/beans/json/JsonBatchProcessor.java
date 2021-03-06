package org.aksw.jena_sparql_api.beans.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.spring.json.ContextProcessorJsonUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.AbstractBatchConfiguration;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonBatchProcessor {
    public static final String ATTR_JOB = "job";
    public static final String ATTR_TASKLET = "$tasklet";

    private AbstractBatchConfiguration config;
    private ApplicationContext mainContext;

    //private JobBuilder jobBuilder;
//    private JobBuilderHelper<?> jobBuilder;

    public void processDefaultJob(Map<String, Object> data) {
        // Check for a job attribute
        Object obj = data.get(ATTR_JOB);
    }


    public Job processJob(Map<String, Object> data) throws Exception {

        // Check if there is a context associated with the job
        Object context = data.get(ContextProcessorJsonUtils.ATTR_CONTEXT);
        AnnotationConfigApplicationContext c = new AnnotationConfigApplicationContext();
        c.setParent(mainContext);
        ContextProcessorJsonUtils.processContext(c, context, new HashMap<String, String>());
        c.refresh();

        JobBuilderFactory jobBuilders = config.jobBuilders();

        processStep(null, data);



        JobBuilder jobBuilder = jobBuilders.get("sparqlExportJob");

        // Process the job context and the step context
        SimpleJobBuilder x = jobBuilder.start((Step)null);

        return null;
    }


    public void processSteps(JobBuilder jobBuilder, List<Object> stepSpecs) {

        boolean isFirstStep = true;

        SimpleJobBuilder jb = null;
        StepBuilder stepBuilder = null;
        for(Object stepSpec : stepSpecs) {
            Step step = processStep(stepBuilder, stepSpec);

            if(isFirstStep) {
                jb = jobBuilder.start(step);
                isFirstStep = false;
            } else {
                jb = jb.next(step);
            }
        }

        Job job = jb.build();
    }

    public Step processStep(StepBuilder stepBuilder, Object data) {
        return null;
    }



//    public Step processStepTasklet(StepBuilder stepBuilder, JsonObject spec) throws Exception {
//        JsonElement d = spec.get(ATTR_TASKLET);
//
//        BeanDefinition taskletDef = ContextProcessorJsonUtils.processBean(d);
//        Tasklet tasklet = null;
//
//        Step result = stepBuilder.tasklet(tasklet).build();
//        return result;
//    }
//
//
//    public Step processStepDefault(StepBuilder stepBuilder, JsonObject spec) throws Exception {
////        Integer chunkSize = MapUtils.getInteger(spec, "chunkSize", 1000);
//
//          ContextProcessorJsonUtils.processBean(spec, "reader");
//
//
////        SimpleStepBuilder<Object, Object> ssb = stepBuilder.chunk(chunkSize);
//
////        ssb.reader();
//        return null;
//    }

}

/*

//        BeanDefinitionRegistry registry;
//        registry.
//
//        ApplicationContext context;
//        context.



        //bf.
        //org.springframework.beans.factory.xml.
        // TODO How to instantiate a bean definition?




    public Step dataCountStep(Query query, QueryExecutionFactory qef) {
        Tasklet tasklet = new TaskletSparqlCountData(query, qef);
        return stepBuilders.get("dataCountStep").tasklet(tasklet).build();
    }

//    @Bean
//    public Step countStep {
//        stepBuilders.get("countStep").tasklet();
//    }

    @Bean
    public Step dataFetchStep() {
        return stepBuilders.get("dataFetchStep").<Binding, Binding> chunk(chunkSize)
                .reader(reader(null, null, null))
                .processor(processor())
                .writer(writer(null, null))
                .build();
    }

*/