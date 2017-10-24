package org.aksw.jena_sparql_api.mapper;

import java.util.Map;

import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.NodeTransformLib;

public class AccDatasetGraph
    implements Acc<DatasetGraph>
{
    private DatasetGraph datasetGraph;
    private QuadPattern quadPattern;

    public AccDatasetGraph(QuadPattern quadPattern) {
       this(DatasetGraphFactory.createGeneral(), quadPattern);
    }

    public AccDatasetGraph(DatasetGraph datasetGraph, QuadPattern quadPattern) {
        super();
        this.datasetGraph = datasetGraph;
        this.quadPattern = quadPattern;
    }

    @Override
    public void accumulate(Binding binding) {
        Map<Var, Node> map = BindingUtils.toMap(binding);
        NodeTransformRenameMap transform = new NodeTransformRenameMap(map);
        QuadPattern inst = NodeTransformLib.transform(transform, quadPattern);

        for(Quad quad : inst) {
        	datasetGraph.add(quad);
        }
    }

    @Override
    public DatasetGraph getValue() {
        return datasetGraph;
    }

}
