package org.aksw.jena_sparql_api.batch;

import com.hp.hpl.jena.sparql.function.FunctionFactory;

import fr.dudie.nominatim.client.NominatimClient;

public class FunctionFactoryGeocodeNominatim
    implements FunctionFactory
{
    private NominatimClient nominatimClient;

    public FunctionFactoryGeocodeNominatim(NominatimClient nominatimClient) {
        this.nominatimClient = nominatimClient;
    }

    @Override
    public com.hp.hpl.jena.sparql.function.Function create(String uri) {
        E_GeocodeNominatim result = new E_GeocodeNominatim(nominatimClient);
        return result;
    }
}