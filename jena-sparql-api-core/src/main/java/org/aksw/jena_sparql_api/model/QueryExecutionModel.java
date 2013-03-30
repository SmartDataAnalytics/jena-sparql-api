package org.aksw.jena_sparql_api.model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 8/3/11
 *         Time: 11:32 PM
 * /
public class QueryExecutionModel
    extends QueryExecutionBase {

    private Model model;

    public QueryExecutionModel(Query query, Dataset dataset, Context context, QueryEngineFactory qeFactory) {
        super(query, dataset, context, qeFactory);
    }

    @Override
    public ResultSet execSelect() {
        return QueryExecutionFactory.crea
    }

}
*/