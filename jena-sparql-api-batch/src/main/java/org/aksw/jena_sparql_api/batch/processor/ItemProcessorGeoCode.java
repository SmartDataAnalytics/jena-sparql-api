package org.aksw.jena_sparql_api.batch.processor;

import java.util.Map.Entry;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.springframework.batch.item.ItemProcessor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.vividsolutions.jts.geom.Geometry;

// We should probably do it like this:
// We give the model as input, and we get a model as output
// and when writing, we need to figure out which prior data existed, to create appropriate diffs.
public class ItemProcessorGeoCode
    implements ItemProcessor<Entry<Resource, Model>, Diff<Model>>
{
    //private NominatimClient client;
    private LookupService<String, Geometry> lookupService;
    
    
    @Override
    public Diff<Model> process(Entry<Resource, Model> subjectToModel) throws Exception {
        //lookupService.
        //List<Address> addresses = client.search("");
        
        
        
        // TODO Auto-generated method stub
        return null;
    }   
}
