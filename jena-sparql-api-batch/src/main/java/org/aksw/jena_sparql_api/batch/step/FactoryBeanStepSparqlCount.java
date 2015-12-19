package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.batch.tasklet.TaskletSparqlCountData;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;

import com.hp.hpl.jena.query.Query;

public class FactoryBeanStepSparqlCount
    extends FactoryBeanStepBase
{
    protected QueryExecutionFactory target;
    protected Query query;

    protected String key;

    public QueryExecutionFactory getTarget() {
        return target;
    }

    public void setTarget(QueryExecutionFactory target) {
        this.target = target;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    protected Step configureStep(StepBuilder stepBuilder) {
        Tasklet tasklet = new TaskletSparqlCountData(query, target, key);

        Step result = stepBuilder.tasklet(tasklet).build();
                //.build();
        return result;
    }


}
