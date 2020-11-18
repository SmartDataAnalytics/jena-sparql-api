package org.aksw.dcat.jena.domain.impl;

import java.util.Collection;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.SetFromMappedPropertyValues;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;

public class DatasetImpl
    extends DcatEntityImpl
    implements DcatDataset
{
    public DatasetImpl(Node node, EnhGraph graph) {
        super(node, graph);
    }

    @Override
    public DcatDistribution createDistribution() {
        return getModel().createResource().as(DcatDistribution.class);
    }

    @Override
    public <T extends Resource> Collection<T> getDistributions(Class<T> clazz) {
        return new SetFromPropertyValues<>(this, DCAT.distribution, clazz); //DcatDistribution.class);
    }

    @Override
    public Collection<String> getKeywords() {
//		return new SetFromLiteralPropertyValues<>(this, DCAT.keyword, String.class);
        return new SetFromMappedPropertyValues<String>(this, DCAT.keyword, NodeMappers.string);
    }


//	@Override
//	public FoafAgent getPublisher() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public VCardKind getContactPoint() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
