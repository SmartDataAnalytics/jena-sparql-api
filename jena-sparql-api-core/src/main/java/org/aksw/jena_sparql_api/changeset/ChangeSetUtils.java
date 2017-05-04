package org.aksw.jena_sparql_api.changeset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlQuery;
import org.aksw.jena_sparql_api.lookup.LookupServiceTransformValue;
import org.aksw.jena_sparql_api.mapper.BindingMapperProjectVar;
import org.aksw.jena_sparql_api.mapper.FunctionBindingMapper;
import org.aksw.jena_sparql_api.update.DiffQuadUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.aksw.jena_sparql_api.utils.SetGraph;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

import com.google.common.base.Function;

// TODO A vocubulary class, name it properly
class V {
    public static final String ns = "http://example.org/";

    public static final Resource RdfDataset = ResourceFactory.createResource(ns + " RdfDataset");

    public static final Property endpoint = ResourceFactory.createProperty(ns + " endpoint");
    public static final Property target = ResourceFactory.createProperty(ns + " target");
    public static final Property username = ResourceFactory.createProperty(ns + "username");
    public static final Property password = ResourceFactory.createProperty(ns + "password");
}

class ResourceUtils {
    public static Resource createSubResource(Resource parent, String subName) {
        String str = parent.getURI() + "/" + subName;
        Model m = parent.getModel();
        Resource result;
        if(m == null) {
            result = ResourceFactory.createResource(str);
        } else {
            result = m.createResource(str);
        }
        return result;
    }
}

public class ChangeSetUtils {

    public static final Query queryMostRecentChangeSet = QueryFactory.parse(new Query(), "Prefix cs: <http://purl.org/vocab/changeset/schema#> Select ?s ?o ?y ?z { ?s cs:subjectOfChange ?o . Optional { ?x cs:precedingChangeSet ?s } . Optional { ?s cs:service ?y } . Optional { ?s cs:graph ?z } .  Filter(!Bound(?x)) }", "http://example.org/", Syntax.syntaxARQ);



    public static String createHash(SparqlServiceReference serviceRef) {
        String result = serviceRef.getServiceURL() + serviceRef.getDefaultGraphURIs() + serviceRef.getNamedGraphURIs();

        result = StringUtils.md5Hash(result);

        return result;
    }

    public static void writeServiceReference(SparqlServiceReference serviceRef, String targetGraph, Resource root, Model model) throws IllegalAccessException {

        Resource dgs = ResourceUtils.createSubResource(root, "defaultGraphs");
        Resource ngs = ResourceUtils.createSubResource(root, "namedGraphs");


        // An update can only go to a single graph
        model.add(root, RDF.type, V.RdfDataset);
        model.add(root, V.endpoint, model.createResource(serviceRef.getServiceURL()));
        model.add(root, V.target, model.createResource(targetGraph));

        UsernamePasswordCredentials credentials = serviceRef.getCredentials();
        //if(auth instanceof SimpleAuthenticator) {
        if(credentials != null) {
            //SimpleAuthenticator a = (SimpleAuthenticator)auth;

            //String username = (String)FieldUtils.readField(a, "username" , true);
            //char[] password = (char[])FieldUtils.readField(a, "password" , true);

            model.add(root, V.username, model.createLiteral(credentials.getUserName()));
            model.add(root, V.password, model.createLiteral(credentials.getPassword()));
        }
    }

    public static LookupService<Node, Node> createLookupServiceMostRecentChangeSet(QueryExecutionFactory qef, Node service, Node graph) {

        Query query = queryMostRecentChangeSet;
        if(service != null || graph != null) {
            query = query.cloneQuery();
        }

        if(service != null) {
            QueryUtils.injectFilter(query, new E_Equals(new ExprVar(Vars.y), NodeValue.makeNode(service)));
            //QueryUtils.injectElement(query, ElementUtils.createElement(new Triple(Vars.s, NodeFactory.createURI("http://purl.org/vocab/changeset/schema#service"), Vars.y)));
                    //query.getProject().add(Vars.y);
                    // new E_Equals(new ExprVar(Vars.y), new ExprVar(graph)));
        }

        if(graph != null) {
            QueryUtils.injectFilter(query, new E_Equals(new ExprVar(Vars.z), NodeValue.makeNode(graph)));
            //QueryUtils.injectElement(query, ElementUtils.createElement(new Triple(Vars.s, NodeFactory.createURI("http://purl.org/vocab/changeset/schema#graph"), Vars.z)));
        }

        LookupService<Node, ResultSetPart> ls = new LookupServiceSparqlQuery(qef, query, Vars.o);

        // We filter by o, but project by s
        LookupService<Node, Node> result =
                LookupServiceTransformValue.create(ls, (k, v) ->
                        FunctionResultSetPartFirstRow.fn.andThen(
                        FunctionBindingMapper.create(BindingMapperProjectVar.create(Vars.s))).apply(v));

        return result;
    }

    /**
     * Check if all triples' subject equal the subjectOfChange
     * @param cs
     * @return
     */
    public static boolean isValid(ChangeSet cs) {
        String str = cs.getSubjectOfChange();
        Node s = NodeFactory.createURI(str);

        boolean isValidAdded = isSubjectOfAllTriples(s, cs.getAddition());
        boolean isValidRemoved = isSubjectOfAllTriples(s, cs.getAddition());

        boolean result = isValidAdded && isValidRemoved;

        return result;
    }

    public static boolean isSubjectOfTriple(Node s, Triple triple) {
        boolean result = triple.getSubject().equals(s);
        return result;
    }

    public static boolean isSubjectOfAllTriples(Node s, Graph g) {
        boolean result = true;

        ExtendedIterator<Triple> it = g.find(Node.ANY, Node.ANY, Node.ANY);
        try {
            while(it.hasNext()) {
                Triple triple = it.next();

                // TODO We could implement this as a filter
                boolean isValid = isSubjectOfTriple(s, triple);
                if(!isValid) {
                    result = false;
                    break;
                }
            }
        } finally {
            it.close();
        }

        return result;
    }

    public static void writeLangMap(Model model, Resource s, Property p, Map<String, String> langToText) {
        if(langToText != null) {
            for(Entry<String, String> entry : langToText.entrySet()) {
                String lang = entry.getKey();
                String text = entry.getValue();

                // TODO Is this check needed?

                Literal o = lang == null || lang.trim().isEmpty()
                        ? model.createLiteral(text)
                        : model.createLiteral(text, lang);

                model.add(s, p, o);
            }
        }
    }

    public static void writeReifiedGraph(Model model, Graph graph, Function<Triple, Node> tripleToSubject) {
        ExtendedIterator<Triple> it = graph.find(Node.ANY, Node.ANY, Node.ANY);
        try {
            while(it.hasNext()) {
                Triple triple = it.next();
                Node s = tripleToSubject.apply(triple);
                writeReifiedTriple(model, s, triple);
            }
        } finally {
            it.close();
        }
    }


    public static void writeReifiedTriple(Model model, Node s, Triple triple) {
        RDFNode tmp = ModelUtils.convertGraphNodeToRDFNode(s, model);
        Resource n = tmp.asResource();
        Statement stmt = ModelUtils.tripleToStatement(model, triple);
        writeReifiedStatement(model, n, stmt);
    }

    public static void writeReifiedStatement(Model model, Resource s, Statement stmt) {
        model.add(s, RDF.type, RDF.Statement);
        model.add(s, RDF.subject, stmt.getSubject());
        model.add(s, RDF.predicate, stmt.getPredicate());
        model.add(s, RDF.object, stmt.getObject());
    }

    public static void add(Model model, Resource s, Property p, RDFNode o)
    {
        if(s != null && p != null && o != null) {
            model.add(s, p, o);
        }
    }

    public static void write(Model model, ChangeSet cs) {
        Resource s = model.createResource(cs.getUri());

        model.add(s, RDF.type, CS.ChangeSet);
        //writeLangMap(model, s, CS.changeReason, cs.getChangeReason());

        ChangeSetMetadata md = cs.getMetadata();

        model.add(s, CS.changeReason, model.createLiteral(md.getChangeReason()));
        model.add(s, CS.createdDate, model.createTypedLiteral(md.getCreatedDate()));
        model.add(s, CS.creatorName, model.createLiteral(md.getCreatorName()));

        model.add(s, CS.subjectOfChange, model.createResource(cs.getSubjectOfChange()));

        if(cs.getService() != null) {
            model.add(s, CS.service, model.createResource(cs.getService()));
        }

        if(cs.getGraph() != null) {
            model.add(s, CS.graph, model.createResource(cs.getGraph()));
        }

        //model.add(s, CS.datasetSet);


        if(cs.getPrecedingChangeSet() != null) {
            model.add(s, CS.precedingChangeSet, model.createResource(cs.getPrecedingChangeSet()));
        }

        String prefix = "http://example.org/";

        for(Triple triple : SetGraph.wrap(cs.getAddition())) {
            String uri = prefix + FN_TripleToMd5.fn.apply(triple);
            Resource o = ResourceFactory.createResource(uri);
            Statement stmt = ModelUtils.tripleToStatement(model, triple);
            writeReifiedStatement(model, o, stmt);
            model.add(s, CS.addition, o);
            model.add(s, CS.statement, o);
        }

        for(Triple triple : SetGraph.wrap(cs.getRemoval())) {
            String uri = prefix + FN_TripleToMd5.fn.apply(triple);
            Resource o = ResourceFactory.createResource(uri);
            Statement stmt = ModelUtils.tripleToStatement(model, triple);
            writeReifiedStatement(model, o, stmt);
            model.add(s, CS.removal, o);
            model.add(s, CS.statement, o);
        }
    }

//    public static UpdateRequest createUpdateRequest(ChangeSetMetadata metadata,
//            QueryExecutionFactory qef,
//            Diff<? extends Iterable<Quad>> diff,
//            String prefix) {
//        Diff<Graph> d = new Diff<Graph>(GraphFactory.createGraphMem(), GraphFactory.createGraphMem(), null);
//
//        for(Quad quad : diff.getAdded()) {
//            d.getAdded().add(quad.asTriple());
//        }
//
//        for(Quad quad : diff.getRemoved()) {
//            d.getRemoved().add(quad.asTriple());
//        }
//
//        UpdateRequest result = createUpdateRequestGraph(metadata, qef, d, prefix);
//        return result;
//    }


    public static UpdateRequest createUpdateRequest(ChangeSetMetadata metadata,
            QueryExecutionFactory qef,
            Diff<? extends Iterable<Quad>> quadDiff,
            String prefix) {

        Map<Node, Diff<Set<Triple>>> diff = DiffQuadUtils.partitionQuads(quadDiff);


        //createUpdateRequest(metadata, qef, diff, prefix, serviceUri, graphUri);


        throw new UnsupportedOperationException();
    }

    public static UpdateRequest createUpdateRequestGraph(ChangeSetMetadata metadata,
            QueryExecutionFactory qef,
            Diff<Set<Triple>> diff,
            String prefix,
            String serviceUri,
            String graphUri) {

        Map<Node, ChangeSet> subjectToChangeSet = createChangeSets(qef, serviceUri, graphUri, metadata, diff, prefix);

        Model model = ModelFactory.createDefaultModel();

        for(ChangeSet cs : subjectToChangeSet.values()) {
            write(model, cs);

            System.out.println("v ===================");
            model.write(System.out, "TURTLE");
            System.out.println("^ -------------------");
        }

        UpdateRequest result = UpdateRequestUtils.createUpdateRequest(model, null);
        return result;
    }


//    public Map<Node, ChangeSet> apply(Diff<Graph> diff) {

    public static Map<Node, ChangeSet> createChangeSets(QueryExecutionFactory qef, String serviceUri, String graphUri, ChangeSetMetadata metadata, Diff<Set<Triple>> diff, String prefix) {
        Node service = NodeUtils.asNullableNode(serviceUri);
        Node graph = NodeUtils.asNullableNode(graphUri);

        LookupService<Node, Node> precedingChangeSetLs = ChangeSetUtils.createLookupServiceMostRecentChangeSet(qef, service, graph);

        Set<Triple> added = diff.getAdded();
        Set<Triple> removed = diff.getRemoved();

        Map<Node, Graph> subjectToAdded = GraphUtils.indexBySubject(added);
        Map<Node, Graph> subjectToRemoved = GraphUtils.indexBySubject(removed);

        Set<Node> subjects = new HashSet<Node>();
        subjects.addAll(subjectToAdded.keySet());
        subjects.addAll(subjectToRemoved.keySet());

        // Perform a lookup of latest changesets for the given subjects and create
        // a writeable copy of the map
        Map<Node, Node> tmp = precedingChangeSetLs.apply(subjects);
        Map<Node, Node> subjectToRecentChangeSet = new HashMap<Node, Node>(tmp);

        Map<Node, ChangeSet> result = new HashMap<Node, ChangeSet>();
        for(Node s : subjects) {
            String subjectOfChange = s.getURI();

            Node precedingId = subjectToRecentChangeSet.get(s);
            String precedingChangeSet = precedingId == null ? null : precedingId.getURI();

            //" " + service + " " + graph + " " +
            String localName = StringUtils.urlEncode(subjectOfChange) + "-" + StringUtils.md5Hash("" + precedingId);
            String uri = prefix + localName;

            // Update the preceding changeset
            Node nextId = NodeFactory.createURI(uri);
            subjectToRecentChangeSet.put(s, nextId);


            Graph addedGraph = subjectToAdded.get(s);
            Graph removedGraph = subjectToRemoved.get(s);

            addedGraph = addedGraph == null ? GraphFactory.createGraphMem() : addedGraph;
            removedGraph = removedGraph == null ? GraphFactory.createGraphMem() : removedGraph;

            ChangeSet cs = new ChangeSet(metadata, uri, precedingChangeSet, subjectOfChange, addedGraph, removedGraph, serviceUri, graphUri);

            result.put(s, cs);
        }

        return result;
    }


    public static void enrichWithSource(Model model, Node g, SparqlServiceReference ssr) {

        model.write(System.out, "TTL");


        Set<Resource> rs = model.listSubjectsWithProperty(RDF.type, CS.ChangeSet).toSet();



        // TODO Auto-generated method stub

    }



//  public static Query createQueryPrecedingChangeSet() {
    //String str = "Prefix cs: <http://purl.org/vocab/changeset/schema#> Select ?s { ?s cs:subjectOfChange <" + uri + "> ; cs:createdDate ?d } Order By Desc(?d) Limit 1";

    // Get the changeset for a subject that does not (yet) occurr as a preceding changeset
    /*
    Query query = new Query();
    query.setQuerySelectType();
    query.getProject().add(Vars.s);

    ElementGroup queryPattern = new ElementGroup()

    ElementTriplesBlock b1 = new ElementTRip

    query.setQueryPattern(queryPattern);
*/

    //String queryString = "{ Select ?s { ?s <http://purl.org/vocab/changeset/schema#subjectOfChange> ?o . Optional { ?x <http://purl.org/vocab/changeset/schema#precedingChangeSet> ?s } . Filter(!Bound(?x)) } Order By Desc(?d) Limit 1 }";
    //Concept concept = Concept.create(queryString, "s");
    //LookupService ls = new LookupServiceSparqlQuery(qef, )

//}

}

