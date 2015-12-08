package org.aksw.jena_sparql_api.mapper.test;

import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;

@DefaultIri("o:Country")
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
