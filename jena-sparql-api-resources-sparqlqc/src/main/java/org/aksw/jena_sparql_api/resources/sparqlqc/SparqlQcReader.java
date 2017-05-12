package org.aksw.jena_sparql_api.resources.sparqlqc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.commons.util.StreamUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.topbraid.spin.vocabulary.SP;


/**
 * The essential method is .loadTasks(rdfFile, queryFolder) which enriches the basic
 * rdf test-case model with data read from the referenced files.
 *
 *
 * SparqlQcReader.loadTestSuites("sparqlqc/1.4/benchmark/cqnoproj.rdf");
 *
 * @author raven
 *
 */
public class SparqlQcReader {
    public static final Pattern queryNamePattern = Pattern.compile("Q(?<id>\\d+)(?<variant>\\w+)");
    public static final Pattern schemaNamePattern = Pattern.compile("C(?<id>\\d+)");

    private static PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();



    /**
     * Core function: Loads all test suites fro m the given resource
     *
     * @param baseFile
     * @return
     * @throws IOException
     */
    public static List<org.apache.jena.rdf.model.Resource> loadTestSuites(String baseFile) throws IOException {
        Model testSuitesModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(testSuitesModel, baseFile, Lang.RDFXML);

        List<org.apache.jena.rdf.model.Resource> result = testSuitesModel.listResourcesWithProperty(RDF.type, SparqlQcVocab.TestSuite).toList();
        for(org.apache.jena.rdf.model.Resource testSuite : result) {
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
    public static List<org.apache.jena.rdf.model.Resource> loadTasks(String baseFile) throws IOException {
        List<org.apache.jena.rdf.model.Resource> testSuites = loadTestSuites(baseFile);

        List<org.apache.jena.rdf.model.Resource> testCases = testSuites.stream()
                .flatMap(testSuite -> testSuite.getProperty(SparqlQcVocab.hasTest).getObject().as(RDFList.class).asJavaList().stream().map(RDFNode::asResource))
                .collect(Collectors.toList());

        return testCases;
    }

    public static void enrichTestCasesWithLabels(Model model) {
        Set<Statement> stmts = model.listStatements(null, RDF.type, SparqlQcVocab.ContainmentTest).toSet();
        for(Statement stmt : stmts) {
            org.apache.jena.rdf.model.Resource s = stmt.getSubject();
            String str = s.getURI();
            int x = str.lastIndexOf('/') + 1;
            String id = str.substring(x).replace('#', '_');
            model.add(s, RDFS.label, id);
        }
    }



    public static void resolveIoResourceRef(
            org.apache.jena.rdf.model.Resource testCase,
            Property property,
            //String basePath,
            Map<String, RDFNode> cache,
            BiFunction<String, Model, RDFNode> rdfizer)
   {
        Statement stmt = testCase.getProperty(property);
        if(stmt != null) {
            String str = stmt.getString();
            //String filePath = basePath + "/" + str;
            //Resource ioResource = new ClassPathResource(filoPath);
            RDFNode res = cache.computeIfAbsent(str, (ref) -> {
                RDFNode r = rdfizer.apply(str, testCase.getModel()); //processQuery(ioResource, testCase.getModel());
                return r;
            });

            testCase.removeAll(property);
            testCase.addProperty(property, res);
        }
    }

    public static void loadTestSuite(org.apache.jena.rdf.model.Resource testSuite, String basePath) {
        Statement stmt = testSuite.getProperty(SparqlQcVocab.sourceDir);

        Map<String, RDFNode> resolvedQueryRef = new HashMap<>();
        Map<String, RDFNode> resolvedSchemaRef = new HashMap<>();


        if(stmt != null) {
            String sourceDir = stmt.getString();
            String resourceFolder = basePath + "/../" + sourceDir;

            List<org.apache.jena.rdf.model.Resource> testCases = testSuite.getProperty(SparqlQcVocab.hasTest).getObject()
                    .as(RDFList.class).asJavaList().stream().map(RDFNode::asResource).collect(Collectors.toList());

            BiFunction<String, Model, RDFNode> queryResolver = (refStr, m) -> {
                String filePath = resourceFolder + "/" + refStr;
                Resource ioResource = new ClassPathResource(filePath);
                RDFNode r = SparqlQcReader.processQueryUnchecked(ioResource, m);
                return r;
            };

            BiFunction<String, Model, RDFNode> schemaResolver = (refStr, m) -> {
                String filePath = resourceFolder + "/" + refStr + ".rdfs";
                Resource ioResource = new ClassPathResource(filePath);
                RDFNode r = SparqlQcReader.processSchemaUnchecked(ioResource, m);
                return r;
            };


            for(org.apache.jena.rdf.model.Resource testCase : testCases) {
                resolveIoResourceRef(testCase, SparqlQcVocab.sourceQuery, resolvedQueryRef, queryResolver);
                resolveIoResourceRef(testCase, SparqlQcVocab.targetQuery, resolvedQueryRef, queryResolver);
                resolveIoResourceRef(testCase, SparqlQcVocab.rdfSchema, resolvedSchemaRef, schemaResolver);
            }
        }
    }



    public static org.apache.jena.rdf.model.Resource processQueryUnchecked(Resource resource, Model inout) {
        try {
            return processQuery(resource, inout);
        } catch(Exception e) {
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

    public static org.apache.jena.rdf.model.Resource createQueryResource(QueryRef qr) {
        org.apache.jena.rdf.model.Resource result = ResourceFactory.createResource("http://ex.org/query/Q" + qr.getId() + qr.getVariant());
        return result;
    }

    public static org.apache.jena.rdf.model.Resource createSchemaResource(Long id) {
        org.apache.jena.rdf.model.Resource result = ResourceFactory.createResource("http://ex.org/schema/C" + id);
        return result;
    }

    public static org.apache.jena.rdf.model.Resource processSchemaUnchecked(Resource resource, Model inout) {
        try {
            return processSchema(resource, inout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static org.apache.jena.rdf.model.Resource processSchema(Resource resource, Model inout) throws IOException {
        String fileName = resource.getFilename();
        Long id = parseSchemaId(fileName);

        org.apache.jena.rdf.model.Resource result = createSchemaResource(id).inModel(inout);

        String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

        inout
            .add(result, RDF.type, SparqlQcVocab.Schema)
            .add(result, SparqlQcVocab.id, inout.createTypedLiteral(id))
            .add(result, LSQ.text, inout.createLiteral(content))
            .add(result, RDFS.label, fileName);
        //ResourceFactory.createProperty("http://iguana.aksw.org/ontology#queryId")
        //System.out.println("" + id + variant);

        return result;
    }

    public static org.apache.jena.rdf.model.Resource processQuery(Resource resource, Model inout) throws IOException {
        String fileName = resource.getFilename();
        QueryRef qr = parseQueryId(fileName);

        org.apache.jena.rdf.model.Resource result = createQueryResource(qr).inModel(inout);

        String content = StreamUtils.toString(resource.getInputStream());

        inout
            .add(result, RDF.type, SP.Query)
            .add(result, SparqlQcVocab.id, inout.createTypedLiteral(qr.id))
            .add(result, LSQ.text, inout.createLiteral(content))
            .add(result, SparqlQcVocab.variant, inout.createLiteral(qr.variant))
            .add(result, RDFS.label, fileName);
        //ResourceFactory.createProperty("http://iguana.aksw.org/ontology#queryId")
        //System.out.println("" + id + variant);

        return result;
    }



    /**
     * Replace the strings of :sourceQuery and :targetQuery with proper uris
     */
    @Deprecated
    public static void fixQueryReferences(Model model, Property property) {
        Set<Statement> stmts = model.listStatements(null, property, (RDFNode)null).toSet();
        for(Statement stmt : stmts) {
            String ref = stmt.getObject().asLiteral().getString();
            QueryRef qr = parseQueryId(ref);
            org.apache.jena.rdf.model.Resource o = createQueryResource(qr).inModel(model);
            model.remove(stmt);
            model.add(stmt.getSubject(), stmt.getPredicate(), o);
        }
    }


    @Deprecated
    public static Model readQueryFolder(String path) throws IOException {
        Resource[] resources = resolver.getResources(path); //"sparqlqc/1.4/benchmark/noprojection/*");

        Model result = ModelFactory.createDefaultModel();

        Arrays.asList(resources).stream()
            .forEach(x -> processQueryUnchecked(x, result));

        return result;
    }

}
