package org.aksw.jena_sparql_api.geo;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.FactoryBean;

import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimClient;

public class NominatimLookupServiceFactory
    implements FactoryBean<LookupServiceNominatim>
{
    private HttpClient httpClient;
    private String email;
    
    
    public NominatimLookupServiceFactory() {
        
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public LookupServiceNominatim getObject() throws Exception {
        HttpClient hc = httpClient != null ? httpClient : new DefaultHttpClient();
        //String em = email != nul ? em : "";
        NominatimClient nominatimClient = new JsonNominatimClient(hc, email);
        
        
        LookupServiceNominatim result = new LookupServiceNominatim(nominatimClient);
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return LookupServiceNominatim.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
