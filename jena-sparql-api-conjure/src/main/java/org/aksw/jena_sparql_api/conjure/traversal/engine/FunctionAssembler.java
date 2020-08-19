package org.aksw.jena_sparql_api.conjure.traversal.engine;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.conjure.traversal.api.OpPropertyPath;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversalSelf;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversalVisitor;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;

import io.reactivex.rxjava3.core.Flowable;

public class FunctionAssembler
    implements OpTraversalVisitor<Function<RDFNode, Set<RDFNode>>>
{

    public static Set<RDFNode> execPath(RDFConnection conn, RDFNode start, Path path) {
        Query query = new Query();
        query.setQuerySelectType();
        query.getProject().add(Vars.s);
        query.setDistinct(true);
        query.setQueryPattern(ElementUtils.createElementPath(start.asNode(), path, Vars.o));
        Model model = start.getModel();


        Flowable<QuerySolution> flowable = conn == null
                ? SparqlRx.execSelect(() -> QueryExecutionFactory.create(query, model))
                : SparqlRx.execSelect(conn, query);

        Set<RDFNode> result = flowable
                .map(qs -> qs.get(Vars.o.getName()))
                .toList().map(LinkedHashSet::new)
                .blockingGet();

        return result;
    }

    @Override
    public Function<RDFNode, Set<RDFNode>> visit(OpPropertyPath op) {
        String str = op.getPropertyPath();
        Path path = PathParser.parse(str, PrefixMapping.Standard);

        return rdfNode -> execPath(null, rdfNode, path);
    }

    @Override
    public Function<RDFNode, Set<RDFNode>> visit(OpTraversalSelf op) {
        return rdfNode -> Collections.singleton(rdfNode);
    }
}
