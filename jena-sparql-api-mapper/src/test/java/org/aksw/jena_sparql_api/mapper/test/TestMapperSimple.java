package org.aksw.jena_sparql_api.mapper.test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.jpa.core.EntityManagerJena;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeCalendar;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.junit.Test;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Model;

public class TestMapperSimple {

	//@Test // TODO Fix and re-enable test
	public void test1() throws ParseException {
        TypeMapper.getInstance().registerDatatype(new RDFDatatypeCalendar());
        RDFDatatype calendarType = TypeMapper.getInstance().getTypeByClass(Calendar.class);
        System.out.println(calendarType);

		Person person = new Person();
		person.setFirstName("John");
		person.setLastName("Doe");
		person.setBirthPlace("Dover");
		//DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		person.setBirthDate(new GregorianCalendar(2000, 0, 0));

		SparqlService sparqlService = FluentSparqlService.forModel().create();
		EntityManagerJena em = new EntityManagerJena(new RdfMapperEngineImpl(sparqlService));
		em.persist(person);

		Model rdf = sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct();
		rdf.write(System.out, "TTL");
	}

}
