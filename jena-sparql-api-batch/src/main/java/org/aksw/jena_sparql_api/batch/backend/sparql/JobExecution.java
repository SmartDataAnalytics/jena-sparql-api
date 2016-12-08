package org.aksw.jena_sparql_api.batch.backend.sparql;

import org.springframework.batch.item.ExecutionContext;

// A mock class for testing the mapper with the ExecutionContext
public class JobExecution {
    protected long id;
    protected ExecutionContext executionContext;
    
    public JobExecution(long id) {
        super();
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public String toString() {
        return "JobExecution [id=" + id + ", executionContext="
                + executionContext + "]";
    }
    
    
}
