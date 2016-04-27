package org.aksw.jena_sparql_api.resources.sparqlqc;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.commons.util.StreamUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


public class SparqlQcReader {
    public static Pattern queryNamePattern = Pattern.compile("Q(?<id>\\d+)(?<variant>\\w+)");

    private static PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();


    public static Model readResources(String path) throws IOException {
        Resource[] resources = resolver.getResources(path); //"sparqlqc/1.4/benchmark/noprojection/*");

        Model result = ModelFactory.createDefaultModel();

        Arrays.asList(resources).stream()
            .forEach(x -> processResourceUnchecked(x, result));

        return result;
    }


    public static org.apache.jena.rdf.model.Resource processResourceUnchecked(Resource resource, Model inout) {
        try {
            return processResource(resource, inout);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static org.apache.jena.rdf.model.Resource processResource(Resource resource, Model inout) throws IOException {
        String fileName = resource.getFilename();
        Matcher m = queryNamePattern.matcher(fileName);
        boolean hasMatches = m.find();
        org.apache.jena.rdf.model.Resource result;
        if(hasMatches) {
            Long id = Long.parseLong(m.group("id"));
            String variant = m.group("variant");

            result = inout.createResource("http://ex.org/query/" + id + "-" + variant);

            String content = StreamUtils.toString(resource.getInputStream());

            inout.add(result, RDF.type, inout.createResource("http://ex.org/ontology/Query"));
            inout.add(result, inout.createProperty("http://ex.org/ontology/id"), inout.createTypedLiteral(id));
            inout.add(result, inout.createProperty("http://ex.org/ontology/content"), inout.createLiteral(content));
            inout.add(result, inout.createProperty("http://ex.org/ontology/variant"), inout.createLiteral(variant));
        } else {
            result = null;
        }
        //System.out.println("" + id + variant);

        return result;
    }
}
