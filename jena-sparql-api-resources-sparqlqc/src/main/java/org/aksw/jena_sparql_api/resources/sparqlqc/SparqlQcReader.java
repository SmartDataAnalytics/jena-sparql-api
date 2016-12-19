package org.aksw.jena_sparql_api.resources.sparqlqc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.commons.util.StreamUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
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


public class SparqlQcReader {
    public static final Pattern queryNamePattern = Pattern.compile("Q(?<id>\\d+)(?<variant>\\w+)");

    private static PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();


    /**
     * Replace the strings of :sourceQuery and :targetQuery with proper uris
     */
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

    public static void enrichTestsWithLabels(Model model) {
    	Set<Statement> stmts = model.listStatements(null, RDF.type, SparqlQcVocab.ContainmentTest).toSet();
    	for(Statement stmt : stmts) {
    		org.apache.jena.rdf.model.Resource s = stmt.getSubject();
    		String str = s.getURI();
    		int x = str.lastIndexOf('/') + 1;
    		String id = str.substring(x).replace('#', '_');
    		model.add(s, RDFS.label, id);
    	}
    }

    public static List<org.apache.jena.rdf.model.Resource> loadTasks(String testCases, String queries) throws IOException {
		Model tests = ModelFactory.createDefaultModel();
		RDFDataMgr.read(tests, new ClassPathResource(testCases).getInputStream(), Lang.RDFXML);
		fixQueryReferences(tests, SparqlQcVocab.sourceQuery);
		fixQueryReferences(tests, SparqlQcVocab.targetQuery);
		enrichTestsWithLabels(tests);

        Model model = SparqlQcReader.readQueryFolder(queries);
        tests.add(model);
        List<org.apache.jena.rdf.model.Resource> result = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();

        return result;
    }


    public static Model readQueryFolder(String path) throws IOException {
        Resource[] resources = resolver.getResources(path); //"sparqlqc/1.4/benchmark/noprojection/*");

        Model result = ModelFactory.createDefaultModel();

        Arrays.asList(resources).stream()
            .forEach(x -> processQueryUnchecked(x, result));

        return result;
    }


    public static org.apache.jena.rdf.model.Resource processQueryUnchecked(Resource resource, Model inout) {
        try {
            return processQuery(resource, inout);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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
}
