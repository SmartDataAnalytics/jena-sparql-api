package org.aksw.jena_sparql_api.mapper.jpa.core;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
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
    protected RdfTypeFactory typeFactory;

    public SparqlEntityManagerFactory() {
        this.prologue = new Prologue(new PrefixMapping2(PrefixMapping.Extended));
        this.scanPackageNames = new LinkedHashSet<>();
    }


    public Prologue getPrologue() {
        return prologue;
    }

    public SparqlEntityManagerFactory setSparqlService(SparqlService sparqlService) {
        this.sparqlService = sparqlService;
        return this;
    }

    public PrefixMapping getPrefixMapping() {
        PrefixMapping result = getPrologue().getPrefixMapping();
        return result;
    }

    public SparqlEntityManagerFactory setPrefixMapping(PrefixMapping prefixMapping) {
        getPrologue().setPrefixMapping(prefixMapping);
        return this;
    }

    public SparqlEntityManagerFactory setNsPrefixes(PrefixMapping other) {
    	getPrologue().getPrefixMapping().setNsPrefixes(other);
    	return this;
    }

    public SparqlEntityManagerFactory setNsPrefix(String prefix, String uri) {
    	getPrologue().getPrefixMapping().setNsPrefix(prefix, uri);
    	return this;
    }

    public SparqlEntityManagerFactory setNsPrefixes(Map<String, String> map) {
    	getPrologue().getPrefixMapping().setNsPrefixes(map);
    	return this;
    }
    
    public Set<String> getScanPackageNames() {
        return scanPackageNames;
    }

    public SparqlEntityManagerFactory addScanPackageName(String packageName) {
        this.scanPackageNames.add(packageName);
        return this;
    }

    public RdfTypeFactory getTypeFactory() {
        return typeFactory;
    }

    public SparqlEntityManagerFactory setTypeFactory(RdfTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        return this;
    }


    @Override
    public RdfEntityManager getObject() throws Exception {

        RdfMapperEngineImpl mapperEngine;
        if(typeFactory == null) {
            mapperEngine = new RdfMapperEngineImpl(sparqlService, prologue);
        } else {
            mapperEngine = new RdfMapperEngineImpl(sparqlService, typeFactory, prologue);
        }


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


        RdfEntityManager result = new EntityManagerImpl(mapperEngine);

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


    public static SparqlEntityManagerFactory newInstance() {
        return new SparqlEntityManagerFactory();
    }
}
