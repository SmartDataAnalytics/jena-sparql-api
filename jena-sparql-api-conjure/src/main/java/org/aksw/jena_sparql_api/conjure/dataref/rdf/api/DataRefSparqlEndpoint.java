package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import java.util.List;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface DataRefSparqlEndpoint
    extends PlainDataRefSparqlEndpoint, DataRef
{
    @HashId
    @Iri("rpif:serviceUrl")
    @IriType
    DataRefSparqlEndpoint setServiceUrl(String serviceUrl);

    @HashId
    @Override
    @Iri("rpif:namedGraph")
    @IriType
    List<String> getNamedGraphs();

    @HashId
    @Override
    @Iri("rpif:defaultGraph")
    @IriType
    List<String> getDefaultGraphs();

    @Override
    default <T> T accept2(DataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }


    default DataRefSparqlEndpoint mutateDefaultGraphs(Consumer<? super List<? super String>> defaultGraphsMutator) {
        defaultGraphsMutator.accept(getDefaultGraphs());
        return this;
    }

    default DataRefSparqlEndpoint mutateNamedGraphs(Consumer<? super List<? super String>> namedGraphsMutator) {
        namedGraphsMutator.accept(getNamedGraphs());
        return this;
    }

//	public static DataRefSparqlEndpoint create(String serviceUrl) {
//		DataRefSparqlEndpoint result = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class)
//				.setServiceUrl(serviceUrl);
//		return result;
//
//	}

    public static DataRefSparqlEndpoint create(Model model, String serviceUrl) {
        DataRefSparqlEndpoint result = model.createResource().as(DataRefSparqlEndpoint.class)
                .setServiceUrl(serviceUrl);
        return result;

    }
}
