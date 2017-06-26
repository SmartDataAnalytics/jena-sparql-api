package org.aksw.jena_sparql_api.resources.sparqlqc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.commons.util.StreamUtils;
//import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.topbraid.spin.vocabulary.SP;

/**
 * The essential method is .loadTasks(rdfFile, queryFolder) which enriches the
 * basic rdf test-case model with data read from the referenced files.
 *
 *
 * SparqlQcReader.loadTestSuites("sparqlqc/1.4/benchmark/cqnoproj.rdf");
 *
 * @author raven
 *
 */
public class SparqlQcReader {
    // TODO There should be a dedicated class for the LSQ vocab
    public static final Property LSQtext = ResourceFactory.createProperty("http://lsq.aksw.org/vocab#text");



    public static final Pattern queryNamePattern = Pattern.compile("Q(?<id>\\d+)(?<variant>\\w+)");
    public static final Pattern schemaNamePattern = Pattern.compile("C(?<id>\\d+)");

    private static PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public static List<Resource> loadTestSuitesSqcf(String baseFile) {
        Model testSuitesModel = RDFDataMgr.loadModel(baseFile, Lang.TURTLE);

        normalizeSqcfModel(testSuitesModel);

        List<Resource> result = testSuitesModel.listSubjectsWithProperty(SparqlQcVocab.hasTest).toList();

        //enrichTestCasesWithLabels(testSuitesModel);

        return result;
    }

    public static List<Resource> extractTasks(Collection<Resource> testSuites) {
        List<Resource> testCases = testSuites.stream()
                .flatMap(testSuite -> testSuite.getProperty(SparqlQcVocab.hasTest).getObject().as(RDFList.class)
                        .asJavaList().stream().map(RDFNode::asResource))
                .filter(task -> !task.hasProperty(RDF.type, SparqlQcVocab.WarmupContainmentTest))
                .collect(Collectors.toList());

        return testCases;
    }

    public static List<Resource> loadTasksSqcf(String baseFile) throws IOException {
        List<Resource> testSuites = loadTestSuitesSqcf(baseFile);
        List<Resource> testCases = extractTasks(testSuites);
        return testCases;
    }


    public static void renameProperty(Model model, Property p, String uri) {
        List<Statement> stmts = model.listStatements(null, p, (RDFNode)null).toList();
        model.remove(stmts);

        for(Statement stmt : stmts) {
            Property newProperty = model.createProperty(uri);
            model.add(stmt.getSubject(), newProperty, stmt.getObject());
        }
    }

    public static void normalizeSqcfModel(Model testSuitesModel) {
        // Transform hasTest to an RDF list
        Property hasTest = ResourceFactory.createProperty("http://sqc-framework.aksw.org/testsuit#hasTest");
        List<Statement> stmts = testSuitesModel.listStatements(null, hasTest, (RDFNode)null).toList();
        testSuitesModel.remove(stmts);

        Model tmpModel = ModelFactory.createDefaultModel();
        tmpModel.add(stmts);

        Set<Resource> suites = stmts.stream().map(Statement::getSubject).collect(Collectors.toSet());
        for(Resource suite : suites) {
            RDFList list = suite.getModel().createList();

            List<RDFNode> testCases = tmpModel.listObjectsOfProperty(suite, hasTest).toList();
            for(RDFNode testCase : testCases) {
                list = list.with(testCase);
            }

            suite.addProperty(SparqlQcVocab.hasTest, list);
        }


        renameProperty(testSuitesModel, ResourceFactory.createProperty("http://sqc-framework.aksw.org/vocab#result"), SparqlQcVocab.result.getURI());
        renameProperty(testSuitesModel, ResourceFactory.createProperty("http://sqc-framework.aksw.org/vocab#subQuery"), SparqlQcVocab.sourceQuery.getURI());
        renameProperty(testSuitesModel, ResourceFactory.createProperty("http://sqc-framework.aksw.org/vocab#superQuery"), SparqlQcVocab.targetQuery.getURI());

        //RDFDataMgr.write(System.out, testSuitesModel, RDFFormat.TURTLE_PRETTY);
    }


    /**
     * Core function: Loads all test suites fro m the given resource
     *
     * @param baseFile
     * @return
     * @throws IOException
     */
    public static List<Resource> loadTestSuites(String baseFile) throws IOException {
        Model testSuitesModel = RDFDataMgr.loadModel(baseFile);
        List<Resource> result = loadTestSuites(testSuitesModel, baseFile);
        return result;
    }


    public static List<Resource> loadTestSuites(Model testSuitesModel, String baseFile) throws IOException {

        List<Resource> result = testSuitesModel
                .listResourcesWithProperty(RDF.type, SparqlQcVocab.TestSuite).toList();
        for (Resource testSuite : result) {
            loadTestSuite(testSuite, baseFile);
        }

        enrichTestCasesWithLabels(testSuitesModel);

        return result;
    }

    /**
     * Convenience function to skip the test-suite level
     *
     * @param baseFile
     * @return
     * @throws IOException
     */
    public static List<Resource> loadTasks(String baseFile) throws IOException {
        List<Resource> testSuites = loadTestSuites(baseFile);

        List<Resource> testCases = extractTasks(testSuites);
//		List<Resource> testCases = testSuites.stream()
//				.flatMap(testSuite -> testSuite.getProperty(SparqlQcVocab.hasTest).getObject().as(RDFList.class)
//						.asJavaList().stream().map(RDFNode::asResource))
//				.filter(task -> !task.hasProperty(RDF.type, SparqlQcVocab.WarmupContainmentTest))
//				.collect(Collectors.toList());

        return testCases;
    }

    public static void enrichTestCasesWithLabels(Model model) {
        Set<Statement> stmts = model.listStatements(null, RDF.type, SparqlQcVocab.ContainmentTest).toSet();
        for (Statement stmt : stmts) {
            Resource s = stmt.getSubject();
            String str = s.getURI();
            int x = str.lastIndexOf('/') + 1;
            String id = str.substring(x).replace('#', '_');
            model.add(s, RDFS.label, id);
        }
    }

    public static void resolveIoResourceRef(Resource testCase, Property property,
            // String basePath,
            Map<String, RDFNode> cache, BiFunction<String, Model, RDFNode> rdfizer) {
        Statement stmt = testCase.getProperty(property);
        if (stmt != null) {
            String str = stmt.getString();
            // String filePath = basePath + "/" + str;
            // Resource ioResource = new ClassPathResource(filoPath);
            RDFNode res = cache.computeIfAbsent(str, (ref) -> {
                RDFNode r = rdfizer.apply(str, testCase.getModel()); // processQuery(ioResource,
                                                                        // testCase.getModel());
                return r;
            });

            testCase.removeAll(property);
            testCase.addProperty(property, res);
        }
    }

    public static void loadTestSuite(Resource testSuite, String basePath) {
        Statement stmt = testSuite.getProperty(SparqlQcVocab.sourceDir);

        Map<String, RDFNode> resolvedQueryRef = new HashMap<>();
        Map<String, RDFNode> resolvedSchemaRef = new HashMap<>();

        if (stmt != null) {
            String sourceDir = stmt.getString();
            String resourceFolder = basePath + "/../" + sourceDir;

            List<Resource> testCases = testSuite.getProperty(SparqlQcVocab.hasTest)
                    .getObject().as(RDFList.class).asJavaList().stream().map(RDFNode::asResource)
                    .collect(Collectors.toList());

            BiFunction<String, Model, RDFNode> queryResolver = (refStr, m) -> {
                String filePath = resourceFolder + "/" + refStr;
                org.springframework.core.io.Resource ioResource = new ClassPathResource(filePath);
                RDFNode r = SparqlQcReader.processQueryUnchecked(ioResource, m);
                return r;
            };

            BiFunction<String, Model, RDFNode> schemaResolver = (refStr, m) -> {
                String filePath = resourceFolder + "/" + refStr + ".rdfs";
                org.springframework.core.io.Resource ioResource = new ClassPathResource(filePath);
                RDFNode r = SparqlQcReader.processSchemaUnchecked(ioResource, m);
                return r;
            };

            for (Resource testCase : testCases) {
                resolveIoResourceRef(testCase, SparqlQcVocab.sourceQuery, resolvedQueryRef, queryResolver);
                resolveIoResourceRef(testCase, SparqlQcVocab.targetQuery, resolvedQueryRef, queryResolver);
                resolveIoResourceRef(testCase, SparqlQcVocab.rdfSchema, resolvedSchemaRef, schemaResolver);
            }
        }
    }

    public static Resource processQueryUnchecked(
            org.springframework.core.io.Resource resource, Model inout) {
        try {
            return processQuery(resource, inout);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Long parseSchemaId(String ref) {
        Matcher m = schemaNamePattern.matcher(ref);
        m.find();
        Long result = Long.parseLong(m.group("id"));

        return result;
    }

    public static QueryRef parseQueryId(String ref) {
        Matcher m = queryNamePattern.matcher(ref);
        m.find();
        Long id = Long.parseLong(m.group("id"));
        String variant = m.group("variant");

        QueryRef result = new QueryRef(id, variant);
        return result;
    }

    public static Resource createQueryResource(QueryRef qr) {
        Resource result = ResourceFactory
                .createResource("http://ex.org/query/Q" + qr.getId() + qr.getVariant());
        return result;
    }

    public static Resource createSchemaResource(Long id) {
        Resource result = ResourceFactory.createResource("http://ex.org/schema/C" + id);
        return result;
    }

    public static Resource processSchemaUnchecked(
            org.springframework.core.io.Resource resource, Model inout) {
        try {
            return processSchema(resource, inout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Resource processSchema(org.springframework.core.io.Resource resource,
            Model inout) throws IOException {
        String fileName = resource.getFilename();
        Long id = parseSchemaId(fileName);

        Resource result = createSchemaResource(id).inModel(inout);

        String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

        inout.add(result, RDF.type, SparqlQcVocab.Schema).add(result, SparqlQcVocab.id, inout.createTypedLiteral(id))
                .add(result, LSQtext, inout.createLiteral(content)).add(result, RDFS.label, fileName);
        // ResourceFactory.createProperty("http://iguana.aksw.org/ontology#queryId")
        // System.out.println("" + id + variant);

        return result;
    }

    public static Resource processQuery(org.springframework.core.io.Resource resource,
            Model inout) throws IOException {
        String fileName = resource.getFilename();
        QueryRef qr = parseQueryId(fileName);

        Resource result = createQueryResource(qr).inModel(inout);

        String content = StreamUtils.toString(resource.getInputStream());

        inout.add(result, RDF.type, SP.Query).add(result, SparqlQcVocab.id, inout.createTypedLiteral(qr.id))
                .add(result, LSQtext, inout.createLiteral(content))
                .add(result, SparqlQcVocab.variant, inout.createLiteral(qr.variant)).add(result, RDFS.label, fileName);
        // ResourceFactory.createProperty("http://iguana.aksw.org/ontology#queryId")
        // System.out.println("" + id + variant);

        return result;
    }

    /**
     * Replace the strings of :sourceQuery and :targetQuery with proper uris
     */
    @Deprecated
    public static void fixQueryReferences(Model model, Property property) {
        Set<Statement> stmts = model.listStatements(null, property, (RDFNode) null).toSet();
        for (Statement stmt : stmts) {
            String ref = stmt.getObject().asLiteral().getString();
            QueryRef qr = parseQueryId(ref);
            Resource o = createQueryResource(qr).inModel(model);
            model.remove(stmt);
            model.add(stmt.getSubject(), stmt.getPredicate(), o);
        }
    }

    @Deprecated
    public static Model readQueryFolder(String path) throws IOException {
        org.springframework.core.io.Resource[] resources = resolver.getResources(path); // "sparqlqc/1.4/benchmark/noprojection/*");

        Model result = ModelFactory.createDefaultModel();

        Arrays.asList(resources).stream().forEach(x -> processQueryUnchecked(x, result));

        return result;
    }

}
