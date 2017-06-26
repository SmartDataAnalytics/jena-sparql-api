package org.aksw.jena_sparql_api.mapper.test.domain;

import org.aksw.commons.collections.NaturalComparator;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.Ordering;

@DefaultIri("http://ex.org/#{name}")
public class Country {
    @Iri("http://ex.org/label")
    private String name;

    @Iri("http://ex.org/population")
    private int population;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    @Override
    public String toString() {
        return "Country [name=" + name + ", population=" + population + "]";
    }
}
