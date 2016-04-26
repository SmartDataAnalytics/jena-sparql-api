package org.aksw.jena_sparql_api.concept_cache.main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.syntax.PatternVars;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class TestSparqlViewCacheVariableRenaming {

    /**
     * Take a set of queries comprised of quads and filters,
     *
     * randomly shuffle their variables
     * @throws IOException
     *
     */
    @Test
    public void testSparqlViewCacheRenaming() throws IOException {
        //Reader reader = new InputStreamRnew FileInputStream(file) //new InputStreamReader(in);
        //List<String> lines = Files.readAllLines(Paths.get(""), encoding);
        Resource r = new ClassPathResource("bgp-queries.json");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8));
        jsonReader.setLenient(true);

        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> queryStrs = gson.fromJson(jsonReader, listType);

        SparqlQueryParser sparqlParser = SparqlQueryParserImpl.create(Syntax.syntaxARQ);
        List<Query> query = queryStrs.stream().map(sparqlParser).collect(Collectors.toList());

        for(String queryStr : queryStrs) {
            System.out.println(queryStr);
        }


    }

    public static void testVariableRenaming(Query query) {
        //query.
    }

}
