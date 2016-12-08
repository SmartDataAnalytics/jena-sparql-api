package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.batch.cli.main.MainBatchWorkflow;
import org.aksw.jena_sparql_api.modifier.Modifier;
import org.aksw.jena_sparql_api.modifier.ModifierDatasetGraphSparqlUpdate;
import org.aksw.jena_sparql_api.stmt.SparqlUpdateParser;
import org.aksw.jena_sparql_api.stmt.SparqlUpdateParserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.update.UpdateRequest;

/**
 * @author raven
 *
 */
@AutoRegistered
public class C_StringToModifierDatasetGraphSparqlUpdate
    implements Converter<String, Modifier<DatasetGraph>> //ModifierDatasetGraphSparqlUpdate>
{
    private SparqlUpdateParser updateParser = SparqlUpdateParserImpl.create(Syntax.syntaxARQ, new Prologue(MainBatchWorkflow.getDefaultPrefixMapping()));


    @Autowired
    public void setUpdateParser(SparqlUpdateParser updateParser) {
        this.updateParser = updateParser;
    }

    public SparqlUpdateParser getUpdateParser() {
        return updateParser;
    }



    public Modifier<DatasetGraph> convert(String str) {
        UpdateRequest updateRequest = updateParser.apply(str);
        ModifierDatasetGraphSparqlUpdate result = new ModifierDatasetGraphSparqlUpdate(updateRequest);
        return result;
    }
}