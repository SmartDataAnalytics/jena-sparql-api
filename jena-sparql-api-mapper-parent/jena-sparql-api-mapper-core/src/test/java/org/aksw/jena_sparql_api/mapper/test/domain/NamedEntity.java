package org.aksw.jena_sparql_api.mapper.test.domain;

import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;

@DefaultIri("r:#{name}")
public class NamedEntity {
    @Iri("rdfs:label")
    protected String name;

    public NamedEntity() {
    }

    public NamedEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NamedEntity [name=" + name + "]";
    }

}
