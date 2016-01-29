package org.aksw.jena_sparql_api.batch.writer;

import java.util.List;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.Assert;

import org.apache.jena.sparql.core.Quad;

public class ItemWriterQuad
    implements ItemWriter<Quad>
{
    private UpdateExecutionFactory uef;
    private boolean isDelete;

    public ItemWriterQuad(UpdateExecutionFactory uef, boolean isDelete) {
        this.uef = uef;
        this.isDelete = isDelete;

        Assert.notNull(uef);
    }

    public UpdateExecutionFactory getUpdateExecutionFactory() {
        return uef;
    }

    public boolean isDelete() {
        return isDelete;
    }

    @Override
    public void write(List<? extends Quad> quads) throws Exception {
        UpdateExecutionUtils.executeUpdateQuads(uef, quads, isDelete);
    }
}
