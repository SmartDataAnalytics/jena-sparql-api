package org.aksw.jena_sparql_api.mapper.test.cases;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.aksw.commons.beans.model.EntityOps;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.jpa.metamodel.MetamodelGenerator;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.TypeDecider;
import org.aksw.jena_sparql_api.mapper.model.TypeDeciderImpl;
import org.aksw.jena_sparql_api.mapper.test.domain.Person;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeCalendar;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

class JobExecution {
}

public class TestTypeDecider extends TestMapperBase {

	@Before
	public void init() {
    	TypeMapper.getInstance().registerDatatype(new RDFDatatypeCalendar());		
	}
	
	@After
	public void reset() {
		TypeMapper.reset();
	}
	
    @Test
    public void test() {
		
        Map<Class<?>, Node> map = TypeDeciderImpl.scan("org.aksw.jena_sparql_api.mapper.test");
        Assert.assertNotEquals(0, map.size());

        TypeDecider typeDecider = new TypeDeciderImpl();

        Person anne = new Person();
        anne.setFirstName("Anne");
        anne.setLastName("Anderson");
        anne.setBirthPlace("Arizona");
        // anne.setBirthDate(new GregorianCalendar(2000, 0, 0));

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setLastName("Bowlin");
        bob.setBirthPlace("Brooklyn");
        // DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        bob.setBirthDate(new GregorianCalendar(2000, 0, 0));

        bob.getTags().put("knows", anne);
        // bob.getTags().put("c", "d");

        entityManager.persist(bob);

        // Relation relation =
        // pathResolver.resolve("tags").resolve("key").getOverallRelation();

        Function<Class<?>, EntityOps> entityOpsFactory = ((RdfTypeFactoryImpl) mapperEngine.getRdfTypeFactory())
                .getEntityOpsFactory();
        MetamodelGenerator mmg = new MetamodelGenerator(entityOpsFactory);

        mmg.apply(Person.class);

        // System.out.println("Relation: " + relation);

        // bob.getTags().clear();
        // bob.getTags().put("x", "y");

        // entityManager.remove(bob);
        // entityManager.persist(bob);

        // mapperEngine.getPersistenceContext().clear()
        // TODO: The find method yet needs to expand the prefix

        bob = entityManager.find(Person.class, "o:John-Doe-Dover");
        System.out.println("Direct entity: " + bob);

        // mapperEngine.merge();
        RdfType rdfType = mapperEngine.getTypeFactory().forJavaType(Person.class);
        Node id = rdfType.getRootNode(bob);
        System.out.println("Allocated ID: " + id);

        Model rdf = sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }")
                .execConstruct();
        rdf.setNsPrefixes(prologue.getPrefixMapping());
        System.out.println("TRIPLES:");
        RDFDataMgr.write(System.out, rdf, RDFFormat.TURTLE);

        
        //mapperEngine.fetch(typeDecider, Collections.singleton(id)).get(id);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> cq = cb.createQuery(Person.class);

        // ParameterExpression<Long> p = cb.parameter(Long.class);

        Root<Person> r = cq.from(Person.class);
        CriteriaQuery<Person> x = cq.select(r);
        // .where(cb.equal(c.get("firstName"), "Anne"))
        // .where(cb.equal(c.get("lastName"), "Anderson"));

        cq.orderBy(cb.desc(r.get("firstName")));

        TypedQuery<Person> query = entityManager.createQuery(x);
        // query.setFirstResult(2);
        // query.setMaxResults(2);
        List<Person> matches = query.getResultList();
        // Person match = query.getSingleResult();

        System.out.println("Result: " + matches);

        // typeDecider.getApplicableTypes(subject);
    }

    public void tmp() {
        Long jobInstanceId = 1l;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<JobExecution> cq = cb.createQuery(JobExecution.class);
        Root<JobExecution> r = cq.from(JobExecution.class);

        cq.select(r).where(cb.equal(r.get("jobInstanceId"), jobInstanceId)).orderBy(cb.desc(r.get("executionId")));
        TypedQuery<JobExecution> query = entityManager.createQuery(cq);
        List<JobExecution> tq = query.getResultList();
    }
}
