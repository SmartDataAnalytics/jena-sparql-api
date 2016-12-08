package org.aksw.jena_sparql_api.mapper.test.manytomany;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.test.common.NamedEntity;


public class Book
    extends NamedEntity
{
    @Iri("o:author")
    protected List<Author> authors = new ArrayList<Author>();

    public Book() {
        super();
    }

    public Book(String name) {
        super(name);
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
}
