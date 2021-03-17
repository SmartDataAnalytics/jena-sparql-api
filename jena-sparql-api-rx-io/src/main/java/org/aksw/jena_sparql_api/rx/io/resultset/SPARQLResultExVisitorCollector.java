package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.stmt.SPARQLResultEx;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;


/**
 * SPARQLResultVisitor implementation that collects all output in in-memory collections.
 * The collected data can be retrieved as a SPARQLResultEx via getResult(outputMode)
 *
 * @author raven
 *
 */
public class SPARQLResultExVisitorCollector
    implements SPARQLResultExVisitor<Void>
{
    protected Collection<Quad> quads;
    protected TableN table;
    protected Collection<JsonObject> jsonItems;

    public SPARQLResultExVisitorCollector(List<Var> tableVars) {
        this(
                new ArrayList<>(), //DatasetFactoryEx.createInsertOrderPreservingDataset(),
                new TableN(tableVars),
                new ArrayList<>());
    }

    public SPARQLResultExVisitorCollector(
            Collection<Quad> quads,
            TableN table,
            Collection<JsonObject> jsonItems) {
        super();
        this.quads = quads;
        this.table = table;
        this.jsonItems = jsonItems;
    }

    public Collection<Quad> getQuads() {
        return quads;
    }

    public TableN getTable() {
        return table;
    }

    public Collection<JsonObject> getJsonItems() {
        return jsonItems;
    }

    public SPARQLResultEx getResult(OutputMode outputMode) {
        SPARQLResultEx result;
        switch(outputMode) {
        case BINDING:
            result = new SPARQLResultEx(table.toResultSet(), () -> {});
            break;
        case QUAD:
            result = SPARQLResultEx.createQuads(quads.iterator(), () -> {});
            break;
        case JSON:
            result = new SPARQLResultEx(jsonItems.iterator(), () -> {});
            break;
        default:
            throw new IllegalStateException("Unknown output mode: " + outputMode);
        }

        return result;
    }

    public ResultSet getResultSet() {
        return table.toResultSet();
    }

    @Override
    public Void onQuads(Iterator<Quad> it) {
        while (it.hasNext()) {
            Quad quad = it.next();
            quads.add(quad);
        }
        return null;
    }

    @Override
    public Void onTriples(Iterator<Triple> it) {
        onQuads(Iterators.transform(it, t -> new Quad(Quad.defaultGraphIRI, t)));
        return null;
    }

    @Override
    public Void onBooleanResult(Boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void onResultSet(ResultSet it) {
        while (it.hasNext()) {
            Binding binding = it.nextBinding();
            table.addBinding(binding);
        }
        return null;
    }

    @Override
    public Void onJsonItems(Iterator<JsonObject> it) {
        while (it.hasNext()) {
            JsonObject json = it.next();
            jsonItems.add(json);
        }
        return null;
    }

}
