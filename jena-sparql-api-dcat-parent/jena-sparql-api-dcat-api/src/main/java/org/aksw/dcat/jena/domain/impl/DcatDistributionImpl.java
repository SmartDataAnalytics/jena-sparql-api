package org.aksw.dcat.jena.domain.impl;

import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromMappedPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;

public class DcatDistributionImpl
	extends DcatEntityImpl
	implements DcatDistribution
{
	public DcatDistributionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	@Override
	public Set<String> getAccessURLs() {
		return new SetFromMappedPropertyValues<>(this, DCAT.accessURL, NodeMappers.DEFAULT_URI_OR_STRING);
	}

	@Override
	public Set<String> getDownloadURLs() {
		return new SetFromMappedPropertyValues<>(this, DCAT.downloadURL, NodeMappers.DEFAULT_URI_OR_STRING);
//		return new SetFromPropertyValues<>(this, DCAT.downloadURL, Resource.class);
	}

	@Override
	public String getFormat() {
		return ResourceUtils.getLiteralPropertyValue(this, DCTerms.format, String.class);
	}

	@Override
	public void setFormat(String format) {
		ResourceUtils.setLiteralProperty(this, DCTerms.format, format);
	}
}
