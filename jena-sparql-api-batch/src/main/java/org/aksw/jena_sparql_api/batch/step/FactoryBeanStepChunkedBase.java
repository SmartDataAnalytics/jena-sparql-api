package org.aksw.jena_sparql_api.batch.step;

public abstract class FactoryBeanStepChunkedBase
    extends FactoryBeanStepBase
{
    int chunk;

    public int getChunk() {
        return chunk;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }
}
