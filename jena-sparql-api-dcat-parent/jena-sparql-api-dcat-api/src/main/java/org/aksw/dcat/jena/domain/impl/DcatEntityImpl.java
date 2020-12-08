package org.aksw.dcat.jena.domain.impl;

import org.aksw.dcat.jena.domain.api.DcatEntity;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.DCTerms;

public class DcatEntityImpl
    extends ResourceImpl
    implements DcatEntity
{
    public DcatEntityImpl(Node node, EnhGraph graph) {
        super(node, graph);
    }


    @Override
    public Resource asResource() {
        return new ResourceImpl(this.node, this.enhGraph);
    }
//	@Override
//	public String getCkanId() {
//		String result = ResourceUtils.getLiteralValue(this, DCTerms.identifier, Literal::getString).orElse(null);
//		return result;
//	}
//
//	@Override
//	public void setCkanId(String identifier) {
//		ResourceUtils.setLiteralValue(this, DCTerms.identifier, String.class, identifier);
//	}

    @Override
    public String getIdentifier() {
        String result = ResourceUtils.getLiteralPropertyValue(this, DCTerms.identifier, String.class);
        return result;
    }

    @Override
    public void setIdentifier(String identifier) {
        ResourceUtils.setLiteralProperty(this, DCTerms.identifier, identifier);
    }

    @Override
    public String getTitle() {
        String result = ResourceUtils.getLiteralPropertyValue(this, DCTerms.title, String.class);
        return result;
    }

    @Override
    public String getDescription() {
        String result = ResourceUtils.getLiteralPropertyValue(this, DCTerms.description, String.class);
        return result;
    }


    @Override
    public void setTitle(String title) {
        ResourceUtils.setLiteralProperty(this, DCTerms.title, title);
    }


    @Override
    public void setDescription(String description) {
        ResourceUtils.setLiteralProperty(this, DCTerms.description, description);
    }
}
