package org.aksw.jena_sparql_api.spring.conversion;

import org.springframework.core.convert.converter.Converter;

import com.hp.hpl.jena.sparql.core.DatasetDescription;


@AutoRegistered
public class C_StringToDatasetDescription
    implements Converter<String, DatasetDescription>
{
    public DatasetDescription convert(String defaultGraphUri) {
        DatasetDescription result = new DatasetDescription();
        result.addDefaultGraphURI(defaultGraphUri);
        return result;
    }
}