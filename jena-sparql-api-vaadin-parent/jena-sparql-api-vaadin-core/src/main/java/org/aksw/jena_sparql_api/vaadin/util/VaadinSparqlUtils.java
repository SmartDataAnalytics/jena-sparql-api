package org.aksw.jena_sparql_api.vaadin.util;

import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparql;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.DataProvider;

public class VaadinSparqlUtils {
    /**
     * Configure a grid's data provider based on a SPARQL SELECT query such that
     * pagination, sorting (TODO: and filtering) works out of the box.
     *
     * @param grid
     * @param qef
     * @param query
     */
    public static void setQueryForGrid(
            Grid<Binding> grid,
            Function<? super Query, ? extends QueryExecution> qef,
            Query query) {
        Relation relation = RelationUtils.fromQuery(query);
        DataProvider<Binding, Expr> dataProvider = new DataProviderSparql(relation, qef);

        grid.setDataProvider(dataProvider);
        List<Var> vars = query.getProjectVars();
        grid.removeAllColumns();

        for (Var var : vars) {
            Column<Binding> column = grid.addColumn(binding -> {
                Node node = binding.get(var);
                Object r;
                if (node == null) {
                    r = null;
                } else if (node.isLiteral()) {
                    r = node.getLiteralValue();
                } else {
                    r = node.toString();
                }
                return r;
            }).setHeader(var.getName());

            column.setKey(var.getName());
            column.setResizable(true);
            column.setSortable(true);
        }
    }
}
