package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Arrays;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import com.google.common.io.BaseEncoding;

public class TestRdfIdGeneration {

    @ResourceView
    public static interface Person extends Resource {
        @HashId
        @Iri("http://xmlns.com/foaf/0.1/firstName")
        String getFirstName();
        Person setFirstName(String firstName);

        @HashId
        @Iri("http://xmlns.com/foaf/0.1/lastName")
        String getLastName();
        Person setLastName(String lastName);

        @HashId
        @Iri("http://xmlns.com/foaf/0.1/age")
        Integer getAge();
        Integer setAge(Integer age);

        @Iri("http://xmlns.com/foaf/0.1/skills")
        Set<String> getSkills();
    }

    @ResourceView
    public static interface Department extends Resource {
        @HashId
        @Iri("eg:member")
        Set<Person> getMembers();
    }

    @Test
    public void test() {
        JenaPluginUtils.registerResourceClasses(Person.class, Department.class);

        Model m = ModelFactory.createDefaultModel();

        Person ana = m.createResource().as(Person.class);
        ana.setFirstName("Ana").setLastName("Ana").setAge(20);

        Person bob = m.createResource().as(Person.class);
        bob.setFirstName("Bob").setLastName("Bob").setAge(20);

        System.out.println("Ana: " + MapperProxyUtils.getHashId(ana).getHashId(ana));
        System.out.println("Bob: " + MapperProxyUtils.getHashId(bob).getHashId(bob));

        bob.getSkills().add("Semantic Web");

        // Turn bob into ana
        bob.setFirstName("Ana").setLastName("Ana").setAge(20);
        System.out.println("Bob as Ana: " + MapperProxyUtils.getHashId(bob).getHashId(bob));


        Department dep = m.createResource().as(Department.class);
        System.out.println("dep: " + MapperProxyUtils.getHashId(dep).getHashId(dep));

        dep.getMembers().addAll(Arrays.asList(ana, bob));
        System.out.println("dep: " + MapperProxyUtils.getHashId(dep).getHashId(dep));

        dep.getMembers().clear();
        System.out.println("dep: " + MapperProxyUtils.getHashId(dep).getHashId(dep));

        System.out.println("Bob's RDF graph: " + bob);

    }
}
