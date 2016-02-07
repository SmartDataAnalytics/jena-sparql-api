package org.aksw.jena_sparql_api_sparql_path2;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sdb.shared.SymbolRegistry;
import org.apache.jena.sdb.store.Feature.Name;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;


public class MainSparqlPath2 {


    public static void main(String[] args) {

        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths());


        Model model = ModelFactory.createDefaultModel();
        String queryStr = "PREFIX jsafn: <http://jsa.aksw.org/fn/> SELECT ?p { ?s jsafn:kShortestPaths ('(<p>/!<p>)*' ?o) }";
        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();

        for(int i = 0; i < 10; ++i) {
            Context context = ARQ.getContext().copy();
            //SymbolRegistry.
            QueryExecutionFactory qef = FluentQueryExecutionFactory.dataset(DatasetFactory.create(), context).create();
            context.put(PropertyFunctionKShortestPaths.PROLOGUE, new Prologue());
            context.put(PropertyFunctionKShortestPaths.QEF, qef);

            QueryExecution qe = qef.createQueryExecution(queryStr);
            //System.out.println("query: " + qe.getQuery());
            ResultSet rs = qe.execSelect();
            ResultSetFormatter.outputAsTSV(System.out, rs);
        }
    }


}
