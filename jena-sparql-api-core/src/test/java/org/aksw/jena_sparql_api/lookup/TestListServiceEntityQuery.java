package org.aksw.jena_sparql_api.lookup;

import java.util.List;

import org.aksw.jena_sparql_api.rx.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.entity.model.AttributeGraphFragment;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

import com.google.common.collect.Range;

public class TestListServiceEntityQuery {

    static { JenaSystem.init(); }

    @Test
    public void testListServiceFromEntityQuery() {
        SparqlQueryParser parser = SparqlQueryParserImpl.create(PrefixMapping.Extended);

        AttributeGraphFragment agf = new AttributeGraphFragment();
        agf.addMandatoryJoin(Vars.s, parser.apply("CONSTRUCT WHERE { ?s eg:type ?t }"));
        agf.addOptionalJoin(Vars.s, parser.apply("CONSTRUCT WHERE { ?s eg:label ?l }"));

        Dataset dataset = DatasetFactory.create();
        try (SparqlQueryConnection conn = RDFConnectionFactory.connect(dataset)) {
            ListService<EntityBaseQuery, RDFNode> listService = new ListServiceEntityQuery(conn::query, agf);

            EntityBaseQuery baseQuery = EntityBaseQuery.create(Vars.x, parser.apply("SELECT * { ?x a ?y } LIMIT 10 OFFSET 5"));
            ListPaginator<RDFNode> paginator = listService.createPaginator(baseQuery);
            System.out.println(paginator.fetchCount(null, null).blockingGet());

            List<RDFNode> nodes = paginator.apply(Range.closedOpen(0l, 10l)).toList().blockingGet();
            System.out.println(nodes);
        }
    }
}
