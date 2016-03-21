package org.aksw.jena_sparql_api.playground;

import java.nio.charset.StandardCharsets;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.engine.binding.Binding;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

public class DatasetMapUtils {
    public static String DCAT_QUERY_DATA_SPARQL;
    public static String DCAT_QUERY_PJS_SPARQL;

    static {
        try {
            DCAT_QUERY_DATA_SPARQL = StreamUtils.copyToString(new ClassPathResource("dcat/query/data-sparql.sparql").getInputStream(), StandardCharsets.UTF_8);
            DCAT_QUERY_PJS_SPARQL = StreamUtils.copyToString(new ClassPathResource("dcat/query/pjs-sparql.sparql").getInputStream(), StandardCharsets.UTF_8);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SparqlServiceReference getSparqlDistribution(QueryExecutionFactory qef, String name) {
        SparqlServiceReference result = coreGetSparqlDistribution(qef, DCAT_QUERY_DATA_SPARQL, name);
        return result;
    }

    public static SparqlServiceReference getPjsDistribution(QueryExecutionFactory qef, String name) {
        SparqlServiceReference result = coreGetSparqlDistribution(qef, DCAT_QUERY_PJS_SPARQL, name);
        return result;
    }



    public static SparqlServiceReference coreGetSparqlDistribution(QueryExecutionFactory qef, String baseQuery, String name) {

        Query query = QueryFactory.create(baseQuery);

        QueryUtils.injectFilter(query, "?l = \"" + name + "\"");

        System.out.println(query);

        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();

        String serviceUri = null;
        //String defaultGraphName;
        DatasetDescription dd = null;
        while(rs.hasNext()) {
            Binding b = rs.nextBinding();
            Node graph = b.get(Vars.g);
            Node service = b.get(Vars.s);

            serviceUri = service == null ? null : service.getURI();
            dd = DatasetDescriptionUtils.createDefaultGraph(graph);
            //defaultGraphName = graph == null ? null : graph.getURI();
        }


        SparqlServiceReference result = serviceUri != null
                ? new SparqlServiceReference(serviceUri, dd)
                : null;
        return result;
    }


    public static SparqlServiceReference getPredicateJoinSummary(Model model, String name) {
        return null;
    }


}
