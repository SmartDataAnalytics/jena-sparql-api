package org.aksw.jena_sparql_api.mapper.test;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.MappedBy;

@DefaultIri("o:department-#{name}")
public class Department {
	@Iri("rdfs:label")
	private String name;

	@MappedBy("department")
	private Set<Employee> employees;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

	@Override
	public String toString() {
		return "Department [name=" + name + ", employees=" + employees + "]";
	}
}

