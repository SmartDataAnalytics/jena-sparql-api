package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.batch.cli.main.AutoRegistered;
import org.springframework.core.convert.converter.Converter;

import com.hp.hpl.jena.sparql.core.DatasetDescription;


@AutoRegistered
public class C_ListOfStringsToDatasetDescription
	implements Converter<String, DatasetDescription>
{
    public DatasetDescription convert(String defaultGraphUri) {
    	DatasetDescription result = new DatasetDescription();
    	result.addDefaultGraphURI(defaultGraphUri);
    	return result;
    }
}