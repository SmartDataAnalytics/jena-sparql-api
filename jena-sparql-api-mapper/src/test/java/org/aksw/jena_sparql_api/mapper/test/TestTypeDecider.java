package org.aksw.jena_sparql_api.mapper.test;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;

import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.TypeDecider;
import org.aksw.jena_sparql_api.mapper.model.TypeDeciderImpl;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeCalendar;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.Test;

public class TestTypeDecider
    extends TestMapperBase
{
    @Test
    public void test() {
        Map<Class<?>, Node> map = TypeDeciderImpl.scan("org.aksw.jena_sparql_api.mapper.test");
        Assert.assertNotEquals(0, map.size());

        TypeDecider typeDecider = new TypeDeciderImpl();

        Person anne = new Person();
        anne.setFirstName("Anne");
        anne.setLastName("Anderson");
        anne.setBirthPlace("Arizona");
        anne.setBirthDate(new GregorianCalendar(2000, 0, 0));
        
        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setLastName("Bowlin");
        bob.setBirthPlace("Brooklyn");
        //DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        bob.setBirthDate(new GregorianCalendar(2000, 0, 0));

        bob.getTags().put("knows", anne);
        //bob.getTags().put("c", "d");
        
        entityManager.persist(bob);
        
        //bob.getTags().clear();
        //bob.getTags().put("x", "y");
        
        entityManager.persist(bob);
        
        //mapperEngine.getPersistenceContext().clear()
        bob = entityManager.find(Person.class, "o:John-Doe-Dover");
        System.out.println("Direct entity: " + bob);


        //mapperEngine.merge();
        RdfType rdfType = mapperEngine.getTypeFactory().forJavaType(Person.class);
        Node id = rdfType.getRootNode(bob);
        System.out.println("Allocated ID: " + id);

        Model rdf = sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct();
        System.out.println("TRIPLES:");
        RDFDataMgr.write(System.out, rdf, RDFFormat.TURTLE);

        mapperEngine.fetch(typeDecider, Collections.singleton(id)).get(id);


        //typeDecider.getApplicableTypes(subject);
    }
}
