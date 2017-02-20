package org.aksw.jena_sparql_api.mapper.test;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.TypeDecider;
import org.aksw.jena_sparql_api.mapper.model.TypeDeciderImpl;
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
        //anne.setBirthDate(new GregorianCalendar(2000, 0, 0));
        
        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setLastName("Bowlin");
        bob.setBirthPlace("Brooklyn");
        //DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        bob.setBirthDate(new GregorianCalendar(2000, 0, 0));

        bob.getTags().put("knows", anne);
        //bob.getTags().put("c", "d");
        
        entityManager.persist(bob);
        
        PathResolver pathResolver = mapperEngine.createResolver(Person.class);
        Relation relation = pathResolver.resolve("tags").resolve("key").getOverallRelation();
        
        
        System.out.println("Relation: " + relation);
        
        //bob.getTags().clear();
        //bob.getTags().put("x", "y");
        
        //entityManager.remove(bob);
        //entityManager.persist(bob);
        
        //mapperEngine.getPersistenceContext().clear()
        // TODO: The find method yet needs to expand the prefix 
        
        bob = entityManager.find(Person.class, "o:John-Doe-Dover");
        System.out.println("Direct entity: " + bob);


        //mapperEngine.merge();
        RdfType rdfType = mapperEngine.getTypeFactory().forJavaType(Person.class);
        Node id = rdfType.getRootNode(bob);
        System.out.println("Allocated ID: " + id);

        Model rdf = sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct();
        rdf.setNsPrefixes(prologue.getPrefixMapping());
        System.out.println("TRIPLES:");
        RDFDataMgr.write(System.out, rdf, RDFFormat.TURTLE);

        mapperEngine.fetch(typeDecider, Collections.singleton(id)).get(id);

                
    	CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    	CriteriaQuery<Person> q = cb.createQuery(Person.class);

    	//ParameterExpression<Long> p = cb.parameter(Long.class);

    	Root<Person> c = q.from(Person.class);
    	CriteriaQuery<Person> x = q.select(c).where(cb.equal(c.get("firstName"), "Anne"));
    	
    	TypedQuery<Person> query = entityManager.createQuery(x);
    	Person match = query.getSingleResult();
    	
    	System.out.println(match);


        //typeDecider.getApplicableTypes(subject);
    }
}
