package org.aksw.jena_sparql_api.vocabs;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * LSQ vocabulary
 *
 * @author Claus Stadler
 *
 */
public class LSQ {
    public static final String ns = "http://lsq.aksw.org/vocab#";

    public static Resource resource(String local) { return ResourceFactory.createResource(ns + local); }
    public static Property property(String local) { return ResourceFactory.createProperty(ns + local); }

    // Used internally for the hypergraph representation - not part of the public vocab
    public static final Resource Vertex = resource(ns + "Vertex");
    public static final Resource Edge = resource(ns + "Edge");

    public static final Property in = property("in");
    public static final Property out = property("out");
    // Internal use. Indicates that one resource represents another one
    public static final Property proxyFor = property("proxyFor");



    public static final Resource Star = resource("Star");
    public static final Resource Sink = resource("Sink");
    public static final Resource Path = resource("Path");
    public static final Resource Hybrid = resource("Hybrid");


    public static final Property joinVertex = property("joinVertex");
    public static final Property joinVertexType = property("joinVertexType");
    public static final Property joinVertexDegree = property("joinVertexDegree");

    public static final Property bgps = property("bgps");
    public static final Property triplePatterns = property("triplePatterns");
    public static final Property minBgpTriples = property("minBgpTriples");
    public static final Property maxBgpTriples = property("maxBgpTriples");
    public static final Property joinVertices = property("joinVertices");

    //public static final Property avgJoinVerticesDegree = property("avgJoinVerticesDegree");
    public static final Property meanJoinVertexDegree = property("meanJoinVertexDegree");
    public static final Property medianJoinVertexsDegree = property("medianJoinVerticesDegree");

    public static final Property mentionsSubject = property("mentionsSubject");
    public static final Property mentionsPredicate = property("mentionsPredicate");
    public static final Property mentionsObject = property("mentionsObject");

    public static final Property mentionsTuple = property("mentionsTuple");


//  stats = stats + getMentionsTuple(predicates); // subjects and objects

// These attributes are part of SPIN - no need to duplicate them
//    public static final Resource Select = resource("Select");
//    public static final Resource Construct = resource("");
//    public static final Resource Ask = resource("");
//    public static final Resource Describe = resource(org.topbraid.spin.vocabulary.SP));



    public static final Property text = property("text");
    public static final Property resultSize = property("resultSize");
    public static final Property hasStructuralFeatures = property("hasStructuralFeatures");
    public static final Property hasSpin = property("hasSpin");
    public static final Property hasTriplePattern = property("hasTriplePattern");
    public static final Property hasTriplePatternExecution = property("hasTriplePatternExecution");
    public static final Property triplePatternText = property("triplePatternText");
    public static final Property triplePatternResultSize = property("triplePatternResultSize");
    public static final Property executionError = property("executionError");
    public static final Property processingError = property("processingError");
    public static final Property parseError = property("parseError");
    public static final Property runTimeMs = property("runTimeMs");

    public static final Property hasExecution = property("hasExecution");
    public static final Property hasLocalExecution = property("hasLocalExecution");
    public static final Property hasRemoteExecution = property("hasRemoteExecution");

    // Execution
    public static final Property triplePatternSelectivity = property("triplePatternSelectivity");
    public static final Property meanTriplePatternSelectivity = property("meanTriplePatternSelectivity");

    // TODO This is PROV vocab
    public static final Property wasAssociatedWith = property("wasAssociatedWith");

    public static final Property usesFeature = property("usesFeature");

    public static final Property triplePath = property("triplePath");

    // TODO Not sure if the following metadata really belongs here ~ Claus
    // TODO This is covered by the MEX ontology
    public static final Property engine = property("engine");
    public static final Property vendor = property("vendor");
    public static final Property version = property("version");
    public static final Property processor = property("processor");
    public static final Property ram = property("ram");

    public static final Property dataset = property("dataset");

    public static final Property endpoint = property("endpoint");


    // TODO This is actually the vocab for apache log parsing - move it elsewhere
    public static final Property host = property("host");
    public static final Property user = property("user");
    public static final Property request = property("request");
    public static final Property query = property("query");
    public static final Property path = property("uri");
    public static final Property protocol = property("protocol");
    public static final Property verb = property("verb");
    public static final Property parsed = property("parsed"); // Whether a log entry could be parsed

    // Query / Graph Pattern Features
    // None indicates the absence of features; must not appear with any other features
    public static final Resource None = resource("None");

    // TODO These terms can be found in SP instead of SPIN
    public static final Resource Select = resource("Select");
    public static final Resource Construct = resource("Construct");
    public static final Resource Describe = resource("Describe");
    public static final Resource Ask = resource("Ask");
    public static final Resource Unknown = resource("Unknown");

    public static final Resource TriplePattern = resource("TriplePattern");
    public static final Resource TriplePath = resource("TriplePath");
    public static final Resource Triple = resource("Triples");
    public static final Resource Group = resource("Group");
    public static final Resource Assign = resource("Assign");
    public static final Resource Dataset = resource("Dataset");
    public static final Resource SubQuery = resource("SubQuery");
    public static final Resource Filter = resource("Filter");
    public static final Resource Values = resource("Values");
    public static final Resource Bind = resource("Bind");
    public static final Resource Service = resource("Service");
    public static final Resource Exists = resource("Exists");
    public static final Resource NotExists = resource("NotExists");
    public static final Resource Minus = resource("Minus");
    public static final Resource NamedGraph = resource("NamedGraph");
    public static final Resource Union = resource("Union");
    public static final Resource Optional = resource("Optional");
    public static final Resource Reduced = resource("Reduced");
    public static final Resource Distinct = resource("Distinct");
    public static final Resource OrderBy = resource("OrderBy");
    public static final Resource GroupBy = resource("GroupBy");
    public static final Resource Aggregators = resource("Aggregators");
    public static final Resource Functions = resource("Functions");
    public static final Resource Offset = resource("Offset");
    public static final Resource Limit = resource("Limit");

    // Path Features
    public static final Resource LinkPath = resource("LinkPath");
    public static final Resource ReverseLinkPath = resource("ReverseLinkPath");
    public static final Resource NegPropSetPath = resource("NegPropSetPath");
    public static final Resource InversePath = resource("InversePath");
    public static final Resource ModPath = resource("ModPath");
    public static final Resource FixedLengthPath = resource("FixedLengthPath");
    public static final Resource DistinctPath = resource("DistinctPath");
    public static final Resource MultiPath = resource("MultiPath");
    public static final Resource ShortestPath = resource("ShortestPath");
    public static final Resource ZeroOrOnePath = resource("ZeroOrOnePath");

    // NOTE: We do not reflect syntactic differences of path expressions such as
    // fooPath{1,} and fooPath+ in the vocab
    public static final Resource ZeroOrMore1Path = resource("ZeroOrMorePath");
    public static final Resource ZeroOrMoreNPath = resource("ZeroOrMorePath");
    public static final Resource OneOrMore1Path = resource("oneOrMorePath");
    public static final Resource OneOrMoreNPath = resource("oneOrMorePath");
    public static final Resource AltPath = resource("AltPath");
    public static final Resource SeqPath = resource("SeqPath");


    public static final Property usesService = property("usesService");


    public static final String defaultLsqrNs = "http://lsq.aksw.org/res/";

    //public static final Property

    //lsqv:resultSize
    //lsqv:runTimeMs
    //lsqv:hasLocalExecution
    //lsqv:structuralFeatures
    //lsqv:runtimeError
    //lsqv:resultSize
    //lsqv:meanTriplePatternSelectivity
    //lsqv:joinVertexDegree 2 ; lsqv:joinVertexType lsqv:Star
    //lsqv:hasRemoteExecution
    //lsqv:endpoint --> maybe supersede by dataset distribution vocab (i think dcat has something)
    //

}
