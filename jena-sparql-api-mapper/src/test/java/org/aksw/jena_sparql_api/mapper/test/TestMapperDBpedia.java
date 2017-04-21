package org.aksw.jena_sparql_api.mapper.test;

import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.annotation.Datatype;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.jpa.core.EntityManagerImpl;
import org.aksw.jena_sparql_api.mapper.model.TypeConversionServiceImpl;
import org.aksw.jena_sparql_api.mapper.model.TypeConverterBase;
import org.aksw.jena_sparql_api.mapper.model.TypeDeciderImpl;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeDelegate;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformDatasetDescription;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformLimit;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.E_DateTimeYear;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;

/**
 * A wrapper around the gYear type for working with integers
 * @author raven
 *
 */
class IntXSDYear
    extends RDFDatatypeDelegate
{

    public IntXSDYear(RDFDatatype delegate) {
        super(XSDDatatype.XSDgYear);
    }

    @Override
    public Class<?> getJavaClass() {
        return Integer.class;
    }



//    @Override
//    public Object parse(String lexicalForm) throws DatatypeFormatException {
//
//    	return super.parse(lexicalForm);
//    }
//    @Override
//    public String unparse(Object value) {
//    	return super.unparse(value);
//    }

}

public class TestMapperDBpedia {


//    public static int getYear(XSDDateTime dt) {
//
//
//
//        XSDDatatype xxx = XSDDatatype.XSDgYear;
//        //xxx.pa
//
//        //NodeFactory.c
//        //XSD.gYear;
//        int result = dt.getYears();
//        return result;
//    }

//    public XSDDateTime createYear(int year) {
//        new XSDDateTime()
//    }


    @RdfType("http://dbpedia.org/ontology/Company")
    @DefaultIri("http://dbpedia.org/resource/#{label}")
    public static class Company {
        @Iri("http://www.w3.org/2000/01/rdf-schema#label")
        private String label;

        @Iri("http://dbpedia.org/ontology/foundingYear")
        @Datatype("http://www.w3.org/2001/XMLSchema#gYear")
        private int foundingYear;

        @Iri("http://dbpedia.org/ontology/numberOfLocations")
        private int numberOfLocations;

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


    @Test
    public void test1() throws ParseException {
        SparqlService sparqlService = FluentSparqlService.http("http://dbpedia.org/sparql", "http://dbpedia.org")
                .config()
                    .configQuery()
                        .withParser(SparqlQueryParserImpl.create())
                    .end()
//                    .withDatasetDescription(dd, graphName)
                    .configQuery()
                        .withQueryTransform(F_QueryTransformDatasetDescription.fn)
                        .withQueryTransform(F_QueryTransformLimit.create(1000))
                    .end()
                .end()
                .create();

        Prologue prologue = new Prologue();
//        prologue.setPrefix("o", "http://example.org/E_DateTimeYearontololgy/");
//        prologue.setPrefix("foaf", FOAF.NS);

        RdfMapperEngineImpl mapperEngine = new RdfMapperEngineImpl(sparqlService, prologue);
        ((TypeDeciderImpl)mapperEngine.getTypeDecider()).put(NodeFactory.createURI("http://dbpedia.org/ontology/Company"), Company.class);


        RdfTypeFactoryImpl tf = (RdfTypeFactoryImpl)mapperEngine.getRdfTypeFactory();
        TypeConversionServiceImpl tcs = (TypeConversionServiceImpl)tf.getTypeConversionService();

        tcs.put(new TypeConverterBase(XSD.gYear.toString(), Integer.class) {
            @Override
            public Expr toJava(Expr expr) {
                return new E_DateTimeYear(expr);
            }

            @Override
            public Node toRdf(Object o) {
                Node node = NodeFactory.createLiteral("" + o, XSDDatatype.XSDgYear);
                return node;
                //NodeValue.makeDatti
                //XSDDatatype.XSDgYear.un
            }
        });


        EntityManager entityManager = new EntityManagerImpl(mapperEngine);


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Company> cq = cb.createQuery(Company.class);

        Root<Company> r = cq.from(Company.class);
        CriteriaQuery<Company> x = cq.select(r)
                .where(cb.equal(r.get("foundingYear"), 2012))
                .where(cb.equal(r.get("numberOfLocations"), 13));

        cq.orderBy(cb.desc(r.get("foundingYear")));

        TypedQuery<Company> query = entityManager.createQuery(x);
        List<Company> matches = query.getResultList();
        // Person match = query.getSingleResult();

        matches.forEach(m -> System.out.println("Result: " + m));


//
//        TypeMapper.getInstance().registerDatatype(new RDFDatatypeCalendar());
//        RDFDatatype calendarType = TypeMapper.getInstance().getTypeByClass(Calendar.class);
    }

}
