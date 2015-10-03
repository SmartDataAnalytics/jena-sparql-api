package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.batch.reader.ItemReaderQuad;
import org.aksw.jena_sparql_api.batch.reader.PredicateQuadExpr;
import org.aksw.jena_sparql_api.batch.writer.ItemWriterQuad;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.google.common.base.Predicate;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.expr.Expr;


public class FactoryBeanStepSparqlPipe
    extends AbstractFactoryBean<Step>
{
    protected StepBuilderFactory stepBuilders;


    protected String name;
    protected int chunkSize;
    protected Query query;

    protected QueryExecutionFactory source;
    protected UpdateExecutionFactory target;

    // A filter expression over the read triples
    protected Expr filter;


    public FactoryBeanStepSparqlPipe() {
        setSingleton(false);
    }

    public StepBuilderFactory getStepBuilders() {
        return stepBuilders;
    }

    @Autowired
    public void setStepBuilders(StepBuilderFactory stepBuilders) {
        this.stepBuilders = stepBuilders;
    }

    public String getName() {
        return name;
    }

    public FactoryBeanStepSparqlPipe setName(String name) {
        this.name = name;
        return this;
    }

    public FactoryBeanStepSparqlPipe setChunk(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public int getChunkSize() {
        return chunkSize;
    }


    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }


    public Query getQuery() {
        return query;
    }


    public void setQuery(Query query) {
        this.query = query;
    }


    public QueryExecutionFactory getSource() {
        return source;
    }


    public void setSource(QueryExecutionFactory source) {
        this.source = source;
    }


    public UpdateExecutionFactory getTarget() {
        return target;
    }


    public void setTarget(UpdateExecutionFactory target) {
        this.target = target;
    }

    public Expr getFilter() {
        return filter;
    }

    public void setFilter(Expr filter) {
        this.filter = filter;
    }

    @Override
    public Step createInstance() throws Exception {

        Predicate<Quad> predicate = filter == null ? null : new PredicateQuadExpr(filter);

        ItemReaderQuad reader = new ItemReaderQuad(source, query, predicate);
        ItemProcessor<? super Quad, ? extends Quad> processor = new PassThroughItemProcessor<Quad>();
        ItemWriterQuad writer = new ItemWriterQuad(target);

        reader.setPageSize(chunkSize);

        //StepBuilderFactory stepBuilders = batchConfig.stepBuilders();
        StepBuilder stepBuilder = stepBuilders.get(name);

        Step result = stepBuilder
                .<Quad, Quad>chunk(chunkSize)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();

        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return Step.class;
    }
}
