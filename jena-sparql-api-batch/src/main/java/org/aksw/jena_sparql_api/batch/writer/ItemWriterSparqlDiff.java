package org.aksw.jena_sparql_api.batch.writer;

import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateDiffUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.apache.jena.atlas.web.HttpException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class ItemWriterSparqlDiff
    implements ItemWriter<Entry<? extends Node, ? extends Diff<? extends DatasetGraph>>>, InitializingBean
{
    private UpdateExecutionFactory uef;

    public ItemWriterSparqlDiff(UpdateExecutionFactory uef) {
        this.uef = uef;
    }

    public UpdateExecutionFactory getUpdateExecutionFactory() {
        return uef;
    }

    public void setUpdateExecutionFactory(UpdateExecutionFactory uef) {
        this.uef = uef;
    }

    @Override
    public void write(List<? extends Entry<? extends Node, ? extends Diff<? extends DatasetGraph>>> items) throws Exception {
        List<Diff<? extends DatasetGraph>> diffs = Lists.newArrayList();

        for(Entry<? extends Node, ? extends Diff<? extends DatasetGraph>> item : items) {
            Diff<? extends DatasetGraph> diff = item.getValue();
            diffs.add(diff);
        }

        Diff<DatasetGraph> diff = UpdateDiffUtils.combineDatasetGraph(diffs);

        try {
            UpdateExecutionUtils.executeUpdateDatasetGraph(uef, diff);
        } catch(Exception e) {
            if(e instanceof HttpException) {
                HttpException x = (HttpException)e;
                String response = x.getResponse();
                throw new RuntimeException(response, e);
            }

        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(uef);
    }

}
