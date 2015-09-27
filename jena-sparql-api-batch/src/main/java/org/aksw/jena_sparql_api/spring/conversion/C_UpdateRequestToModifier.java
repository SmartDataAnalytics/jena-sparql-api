package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.modifier.Modifier;
import org.aksw.jena_sparql_api.modifier.ModifierDatasetGraphSparqlUpdate;
import org.springframework.core.convert.converter.Converter;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.update.UpdateRequest;

@AutoRegistered
public class C_UpdateRequestToModifier
    implements Converter<UpdateRequest, Modifier<DatasetGraph>>
{
    public Modifier<DatasetGraph> convert(UpdateRequest updateRequest) {
        ModifierDatasetGraphSparqlUpdate result = new ModifierDatasetGraphSparqlUpdate(updateRequest);
        return result;
    }
}