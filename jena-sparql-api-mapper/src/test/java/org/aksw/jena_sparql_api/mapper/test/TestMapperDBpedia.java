package org.aksw.jena_sparql_api.mapper.test;

import java.text.ParseException;
import java.util.Calendar;

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
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.E_DateTimeYear;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.PrefixMapping2;
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


    @RdfType("schema:Person")
    @DefaultIri("dbr:#{name}")
    public static class Person {
        @Iri("rdfs:label")
        private String name;

        @Iri("dbo:birthDate")
        private Calendar birthDate;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Calendar getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Calendar birthDate) {
            this.birthDate = birthDate;
        }
    }


    @RdfType("dbo:Company")
    @DefaultIri("dbr:#{label}")
    public static class Company {

        //@Lang("en")
        @Iri("rdfs:label")
        private String label;

        @Iri("dbo:foundingYear")
        @Datatype("xsd:gYear")
        private int foundingYear;

        @Iri("dbo:numberOfLocations")
        private int numberOfLocations;

//        @Iri("dbo:keyPerson")
//        private Set<Person> keyPersons;

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

//        public Set<Person> getKeyPersons() {
//            return keyPersons;
//        }
//
//        public void setKeyPersons(Set<Person> keyPersons) {
//            this.keyPersons = keyPersons;
//        }

//        @Override
//        public String toString() {
//            return "Company [label=" + label + ", foundingYear=" + foundingYear + ", numberOfLocations="
//                    + numberOfLocations + ", keyPersons=" + keyPersons + "]";
//        }

    }


    @Test
    public void test1() throws ParseException {
        Prologue prologue = new Prologue(new PrefixMapping2(PrefixMapping.Extended));
        prologue.setPrefix("schema", "http://schema.org/");
        prologue.setPrefix("dbo", "http://dbpedia.org/ontology/");
        prologue.setPrefix("dbr", "http://dbpedia.org/resource/");

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


        RdfMapperEngineImpl mapperEngine = new RdfMapperEngineImpl(sparqlService, prologue);
        ((TypeDeciderImpl)mapperEngine.getTypeDecider()).putAll(TypeDeciderImpl.scan(TestMapperDBpedia.class.getPackage().getName(), prologue));
        //NodeFactory.createURI("http://dbpedia.org/ontology/Company"), Company.class);


        RdfTypeFactoryImpl tf = (RdfTypeFactoryImpl)mapperEngine.getRdfTypeFactory();
        TypeConversionServiceImpl tcs = (TypeConversionServiceImpl)tf.getTypeConversionService();

        tcs.put(new TypeConverterBase(XSD.gYear.toString(), int.class) {
            @Override
            public Expr toJava(Expr expr) {
                return new E_DateTimeYear(expr);
            }

            @Override
            public Node toRdf(Object o) {
                Node node = NodeFactory.createLiteral("" + o, XSDDatatype.XSDgYear);
                return node;
            }
        });


//        tcs.put(new TypeConverterBase(XSD.xstring.toString(), String.class) {
//            @Override
//            public Expr toJava(Expr expr) {
//                return new E_Str(expr);
//            }
//
//            @Override
//            public Node toRdf(Object o) {
//                Node node = NodeFactory.createLiteral("" + o, XSDDatatype.XSDstring);
//                return node;
//            }
//        });


        EntityManager entityManager = new EntityManagerImpl(mapperEngine);


//        {
//	        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//	        CriteriaQuery<Company> cq = cb.createQuery(Company.class);
//
//	        Root<Company> r = cq.from(Company.class);
//	        CriteriaQuery<Company> x = cq.select(r)
//	                .where(cb.greaterThanOrEqualTo(r.get("foundingYear"), 1955))
//	                .where(cb.greaterThanOrEqualTo(r.get("numberOfLocations"), 36000))
//	                ;
//
//	        TypedQuery<Company> query = entityManager.createQuery(x);
//	        List<Company> matches = query.getResultList();
//	        matches.forEach(m -> System.out.println("Result: " + m));
//        }

        {
            CriteriaBuilder cb2 = entityManager.getCriteriaBuilder();
            CriteriaQuery<Double> cq2 = cb2.createQuery(Double.class);
            Root<Company> r2 = cq2.from(Company.class);
            CriteriaQuery<Double> x2 = cq2.select(cb2.avg(r2.get("numberOfLocations")));
//                    .where(cb2.greaterThanOrEqualTo(r2.get("foundingYear"), 1955))
//                    .where(cb2.greaterThanOrEqualTo(r2.get("numberOfLocations"), 36000))
//                    ;

            TypedQuery<Double> tq2 = entityManager.createQuery(x2);
            Double m2 = tq2.getSingleResult();
            System.out.println("Avg: " + m2);
            //List<Double> m2 = query.getResultList();
            //matches.forEach(m -> System.out.println("avg: " + m));
        }
        //cq.orderBy(cb.desc(r.get("foundingYear")));

        // Person match = query.getSingleResult();



//
//        TypeMapper.getInstance().registerDatatype(new RDFDatatypeCalendar());
//        RDFDatatype calendarType = TypeMapper.getInstance().getTypeByClass(Calendar.class);
    }

}
