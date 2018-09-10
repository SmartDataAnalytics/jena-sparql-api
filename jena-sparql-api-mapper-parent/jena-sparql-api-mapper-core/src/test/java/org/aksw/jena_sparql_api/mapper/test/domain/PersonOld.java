package org.aksw.jena_sparql_api.mapper.test.domain;

import java.util.Calendar;

import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;

// TODO Remove this class
@RdfType("schema:Person")
@DefaultIri("dbr:#{name}")
public class PersonOld {
    @Iri("rdfs:label")
    private String name;

    @Iri("dbo:birthDate")
    private Calendar birthDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Calendar birthDate) {
        this.birthDate = birthDate;
    }
}
