package org.aksw.jena_sparql_api.batch;

import java.io.IOException;
import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.json.NodeValueJson;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

import fr.dudie.nominatim.client.NominatimClient;
import fr.dudie.nominatim.model.Address;

public class E_GeocodeNominatim
    extends FunctionBase1
{
    private NominatimClient nominatimClient;

    public E_GeocodeNominatim(NominatimClient nominatimClient) {
        this.nominatimClient = nominatimClient;
    }

    @Override
    public NodeValue exec(NodeValue v) {
        NodeValue result;
        if(v.isString()) {
            String locationString = v.getString();
            List<Address> addresses;
            try {
                addresses = nominatimClient.search(locationString);
//                if(addresses.isEmpty()) {
//                    result = NodeValue.nvNothing;
//                } else {
//                    result = NodeValue.makeInteger(addresses.get(0).getOsmId());
//                }
                result = new NodeValueJson(addresses);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            result = NodeValue.nvNothing;
        }

        return result;
    }
}