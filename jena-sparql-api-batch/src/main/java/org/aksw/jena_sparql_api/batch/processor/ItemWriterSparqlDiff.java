package org.aksw.jena_sparql_api.batch.processor;

import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateDiffUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.hp.hpl.jena.sparql.core.Quad;

public class ItemWriterSparqlDiff
    implements ItemWriter<Diff<Set<Quad>>>, InitializingBean
{
    private UpdateExecutionFactory uef;

    public ItemWriterSparqlDiff() {
        
    }
    
    public UpdateExecutionFactory getUpdateExecutionFactory() {
        return uef;
    }

    public void setUpdateExecutionFactory(UpdateExecutionFactory uef) {
        this.uef = uef;
    }

    @Override
    public void write(List<? extends Diff<Set<Quad>>> diffs) throws Exception {
        Diff<Set<Quad>> diff = UpdateDiffUtils.combine(diffs);
        
        UpdateExecutionUtils.executeUpdate(uef, diff);        
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(uef);
    }

}
