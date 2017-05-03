package org.aksw.jena_sparql_api.mapper.jpa.core;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.model.TypeConversionServiceImpl;
import org.aksw.jena_sparql_api.mapper.model.TypeConverterBase;
import org.aksw.jena_sparql_api.mapper.model.TypeDeciderImpl;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.E_DateTimeYear;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.apache.jena.vocabulary.XSD;
import org.springframework.beans.factory.FactoryBean;

public class SparqlEntityManagerFactory
    implements FactoryBean<EntityManager>
{
    protected Prologue prologue;
    protected Set<String> scanPackageNames;

    protected SparqlService sparqlService;

    public SparqlEntityManagerFactory() {
        this.prologue = new Prologue(new PrefixMapping2(PrefixMapping.Extended));
        this.scanPackageNames = new LinkedHashSet<>();
    }


    public Prologue getPrologue() {
        return prologue;
    }

    public void setSparqlService(SparqlService sparqlService) {
        this.sparqlService = sparqlService;
    }

    public PrefixMapping getPrefixMapping() {
        PrefixMapping result = getPrologue().getPrefixMapping();
        return result;
    }

    public Set<String> getScanPackageNames() {
        return scanPackageNames;
    }

    public void addScanPackageName(String packageName) {
        this.scanPackageNames.add(packageName);
    }

    @Override
    public EntityManager getObject() throws Exception {

        RdfMapperEngineImpl mapperEngine = new RdfMapperEngineImpl(sparqlService, prologue);

        for(String scanPackageName : scanPackageNames) {

            ((TypeDeciderImpl)mapperEngine.getTypeDecider()).putAll(TypeDeciderImpl.scan(scanPackageName, prologue));
        }

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


        EntityManager result = new EntityManagerImpl(mapperEngine);

        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return EntityManager.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
