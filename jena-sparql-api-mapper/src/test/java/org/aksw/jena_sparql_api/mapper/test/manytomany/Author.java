package org.aksw.jena_sparql_api.mapper.test.manytomany;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.MappedBy;
import org.aksw.jena_sparql_api.mapper.test.common.NamedEntity;

public class Author
    extends NamedEntity
{
    protected List<Book> books = new ArrayList<Book>();

    public Author() {
        super();
    }

    public Author(String name) {
        super(name);
    }

    @MappedBy("author")
    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
