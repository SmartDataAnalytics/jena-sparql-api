package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.mapper.MappedQuery;
import org.aksw.jena_sparql_api.mapper.MappedQueryUtils;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;


@AutoRegistered
public class C_StringToMappedQuery
    implements Converter<String, MappedQuery<DatasetGraph>>
{
    @Autowired
    protected SparqlQueryParser parser;

    public MappedQuery<DatasetGraph> convert(String str) {


        PartitionedQuery1 partQuery = parse(str, parser);
        MappedQuery<DatasetGraph> result = MappedQueryUtils.fromConstructQuery(partQuery);

        return result;
    }


    public static PartitionedQuery1 parse(String str, SparqlQueryParser parser) {
        String[] splits = str.split("\\|", 2);
        if(splits.length != 2) {
            throw new RuntimeException("Invalid string: " + str);
        }

        Var var = VarUtils.parseVar(splits[0]);
        Query query = parser.apply(splits[1]);

        PartitionedQuery1 result = new PartitionedQuery1(query, var);
        return result;
    }

}
