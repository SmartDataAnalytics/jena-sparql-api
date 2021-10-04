package org.aksw.jena_sparql_api.schema;

import java.util.Iterator;

import org.aksw.jena_sparql_api.entity.graph.metamodel.RMM;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceMetamodel;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlQuery;
import org.aksw.jena_sparql_api.mapper.proxy.MapperProxyUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmtMgr;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;



public class ResourceExplorer
{
    protected LookupService<Node, ResourceMetamodel> metamodelLookup;


    public static LookupService<Node, ResourceMetamodel> createMetamodelLookup(SparqlQueryConnection conn) {
        Query query = SparqlStmtMgr.loadQuery("resource-criticalmodel-defaultgraph.rq");
        Var var = Var.alloc("src");


        Template template = query.getConstructTemplate();
        Query selectQuery = query.cloneQuery();
        selectQuery.setQuerySelectType();


        LookupService<Node, ResourceMetamodel> result = new LookupServiceSparqlQuery(conn, selectQuery, var)
            .mapValues((node, table) -> {
                Model m = ModelFactory.createDefaultModel();
                Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), table.rows());
                GraphUtil.add(m.getGraph(), it);

                // The resource we are looking is related to the resource
                Resource r = m.wrapAsResource(node);
                return r;
            })
            .mapValues((node, targetResource) -> {
                ResourceMetamodel r = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils
                        .getReversePropertyValue(targetResource, RMM.targetResource, ResourceMetamodel.class);
                return r;
            })
            .mapValues((node, r) -> MapperProxyUtils.skolemize("http://www.example.org/", r).as(ResourceMetamodel.class));


        return result;
    }
}
