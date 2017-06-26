package org.aksw.jena_sparql_api.mapper.test.cases;

import java.util.Arrays;

import org.aksw.jena_sparql_api.mapper.test.domain.Author;
import org.aksw.jena_sparql_api.mapper.test.domain.Book;
import org.junit.Test;

public class TestMapperManyToMany1
    extends TestMapperBase
{
    //@Test
    public void testMapperManyToMany1_a() {
        Author a1 = new Author("John");
        Author a2 = new Author("Jane");

        Book b1 = new Book("BookOfJohn");
        b1.setAuthors(Arrays.asList(a1));

        Book b2 = new Book("BookOfJane");
        b2.setAuthors(Arrays.asList(a2));

        Book b3 = new Book("BookOfJohnAndJane");
        b3.setAuthors(Arrays.asList(a1, a2));

        entityManager.persist(b1);
        entityManager.persist(b2);
        entityManager.persist(b3);


        sparqlService
            .getQueryExecutionFactory()
            .createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }")
            .execConstruct()
            .write(System.out, "TTL");

        //entityManager.
        //System.out.println("TEST: " + entityManager);
    }
}
