package org.aksw.jena_sparql_api.mapper.test.domain;

import org.aksw.jena_sparql_api.mapper.annotation.Datatype;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;

@RdfType("dbo:Company")
@DefaultIri("dbr:#{label}")
public class Company {

    //@Lang("en")
    @Iri("rdfs:label")
    private String label;

    @Iri("dbo:foundingYear")
    @Datatype("xsd:gYear")
    private int foundingYear;

    @Iri("dbo:numberOfLocations")
    private int numberOfLocations;

//    @Iri("dbo:keyPerson")
//    private Set<Person> keyPersons;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getFoundingYear() {
        return foundingYear;
    }

    public void setFoundingYear(int foundingYear) {
        this.foundingYear = foundingYear;
    }

    public int getNumberOfLocations() {
        return numberOfLocations;
    }

    public void setNumberOfLocations(int numberOfLocations) {
        this.numberOfLocations = numberOfLocations;
    }

    @Override
    public String toString() {
        return "Company [label=" + label + ", foundingYear=" + foundingYear + ", numberOfLocations="
                + numberOfLocations + "]";
    }
}