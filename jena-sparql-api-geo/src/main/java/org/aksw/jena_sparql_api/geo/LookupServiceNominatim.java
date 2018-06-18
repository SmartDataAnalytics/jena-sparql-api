package org.aksw.jena_sparql_api.geo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.dudie.nominatim.client.NominatimClient;
import fr.dudie.nominatim.model.Address;
import io.reactivex.Flowable;

public class LookupServiceNominatim
    implements LookupService<String, List<Address>>
{
    private static final Logger logger = LoggerFactory.getLogger(LookupServiceNominatim.class);
    
    private NominatimClient client;
    
    public LookupServiceNominatim(NominatimClient client) {
        this.client = client;
    }

    @Override
    public Flowable<Entry<String, List<Address>>> apply(Iterable<String> locationStrings) {
        Map<String, List<Address>> result = new HashMap<String, List<Address>>();
        for(String locationString : locationStrings) {
            try {
                List<Address> addresses = client.search(locationString);
                result.put(locationString, addresses);
            } catch(Exception e) {
                logger.warn("Failed nominatim request", e);
            }
        }
        
        return Flowable.fromIterable(result.entrySet());
        //return result;
    }
}
