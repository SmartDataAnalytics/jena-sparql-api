package org.aksw.jena_sparql_api.batch.writer;

import java.util.List;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.Assert;

import com.hp.hpl.jena.sparql.core.Quad;

public class ItemWriterQuad
    implements ItemWriter<Quad>
{
    private UpdateExecutionFactory uef;

    public ItemWriterQuad(UpdateExecutionFactory uef) {
        this.uef = uef;

        Assert.notNull(uef);
    }

    public UpdateExecutionFactory getUpdateExecutionFactory() {
        return uef;
    }

    @Override
    public void write(List<? extends Quad> quads) throws Exception {
        UpdateExecutionUtils.executeInsertQuads(uef, quads);
    }
}
