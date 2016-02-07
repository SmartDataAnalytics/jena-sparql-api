package org.aksw.jena_sparql_api_sparql_path2;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sdb.shared.SymbolRegistry;
import org.apache.jena.sdb.store.Feature.Name;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;


public class MainSparqlPath2 {


    public static void main(String[] args) {

        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths());


        Model model = ModelFactory.createDefaultModel();
        String queryStr = "SELECT ?path { <http://dbpedia.org/resource/Leipzig> jsafn:kShortestPaths ('(rdf:type)*' ?path) }";
        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();

        for(int i = 0; i < 1; ++i) {
            Context context = ARQ.getContext().copy();
            //SymbolRegistry.
            PrefixMappingImpl pm = new PrefixMappingImpl();
            pm.setNsPrefix("jsafn", "http://jsa.aksw.org/fn/");
            pm.setNsPrefixes(PrefixMapping.Extended);
            Prologue prologue = new Prologue(pm);
            QueryExecutionFactory qef = FluentQueryExecutionFactory
                    .dataset(DatasetFactory.create(), context)
                    .config()
                        .withParser(SparqlQueryParserImpl.create(Syntax.syntaxARQ, prologue))
                        .withPrefixes(pm, true)
                    .end()
                    .create();


            context.put(PropertyFunctionKShortestPaths.PROLOGUE, prologue);
            context.put(PropertyFunctionKShortestPaths.QEF, qef);

            QueryExecution qe = qef.createQueryExecution(queryStr);
            //System.out.println("query: " + qe.getQuery());
            System.out.println("Result");
            ResultSet rs = qe.execSelect();
            System.out.println(ResultSetFormatter.asText(rs));
            //ResultSetFormatter.outputAsTSV(System.out, rs);
        }
    }


}
