package org.aksw.jena_sparql_api.batch.step;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class FactoryBeanSimpleJob
	extends AbstractFactoryBean<Job>
{
	@Autowired
	protected JobBuilderFactory jobBuilders;

	protected String name;
	protected List<Step> steps = new ArrayList<Step>();


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	@Override
	public Class<?> getObjectType() {
		return Job.class;
	}

	@Override
	protected Job createInstance() throws Exception {
		if(steps.isEmpty()) {
			throw new RuntimeException("Job does not have any steps");
		}

		JobBuilder jobBuilder = jobBuilders.get(name);

		Step firstStep = steps.get(0);
		SimpleJobBuilder flow = jobBuilder.start(firstStep);
		for(int i = 1; i < steps.size(); ++i) {
			Step step = steps.get(i);
			flow.next(step);
		}
		Job result = flow.build();

		//JobBuilder jobBuilder = jobBuilders.get(null);
		//jobBuilder.st
//		SimpleJob result = new SimpleJob(name);
//		result.setSteps(steps);

		// TODO Auto-generated method stub
		return result;
	}
	//protected AbstractBatchConfiguration batchConfig;
	//protected StepBuilderFactory stepBuilders;

}
