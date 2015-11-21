package org.aksw.jena_sparql_api.mapper.jpa;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateDiffUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.CriteriaBuilderJena;
import org.aksw.jena_sparql_api.mapper.proxy.MethodInterceptorRdf;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Quad;

public class EntityManagerJena
    implements EntityManager
{
    protected Prologue prologue;

    protected RdfTypeFactoryImpl rdfClassFactory;

    /**
     * The languagePreferences acts as a default method to priorize and filter items in a set of (literal)
     * nodes, such as values of the rdfs:label or rdfs:comment properties.
     *
     */
    protected List<String> readLangs;

    /**
     * The default language which to apply to newly created RDF data
     */
    protected String writeLang;
    protected SparqlService sparqlService;


    public EntityManagerJena(RdfTypeFactoryImpl rdfClassFactory,
            List<String> readLangs, SparqlService sparqlService) {
        super();
        //this.prologue = prologue;
        this.rdfClassFactory = rdfClassFactory;
        this.readLangs = readLangs;
        this.sparqlService = sparqlService;
    }


    public RdfTypeFactoryImpl getRdfClassFactory() {
        return rdfClassFactory;
    }

    @Override
    public <T> T find(Class<T> clazz, Object primaryKey) {

        //RdfClassFactory rdfClassFactory = RdfClassFactory.createDefault(prologue);
        RdfClass rdfClass = (RdfClass)rdfClassFactory.forJavaType(clazz);

        MappedConcept<DatasetGraph> shape = rdfClass.getMappedQuery();

        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        LookupService<Node, DatasetGraph> ls = LookupServiceUtils.createLookupService(qef, shape);

        Node node;
        if(primaryKey instanceof String) {
            node = NodeFactory.createURI((String)primaryKey);
        } else if(primaryKey instanceof Node) {
            node = (Node)primaryKey;
        } else {
            throw new RuntimeException("Invalid primary key type: " + primaryKey);
        }

        Map<Node, DatasetGraph> nodeToGraph = ls.apply(Collections.singleton(node));
        DatasetGraph dg = nodeToGraph.get(node);
        if(dg == null) {
            dg = DatasetGraphFactory.createMem();
        }
        //System.out.println(dg);


        //Object proxy = rdfClass.createProxy(dg, node);
        Object proxy = rdfClass.createJavaObject(node);

        rdfClass.setValues(proxy, dg);


        T result = (T)proxy;
        //List<T> result = Collections.<T>singletonList(item);
        return result;
    }

    @Override
    public void persist(Object entity) {
        merge(entity);
    }

    @Override
    public <T> T merge(T entity) {
        MethodInterceptorRdf interceptor = RdfClass.getMethodInterceptor(entity);

        DatasetGraph oldState = interceptor == null
                ? DatasetGraphFactory.createMem()
                : interceptor.getDatasetGraph()
                ;

        Class<?> clazz = entity.getClass();
        //RdfClass rdfClass = RdfClassFactory.createDefault(prologue).create(clazz);
        RdfClass rdfClass = (RdfClass)rdfClassFactory.forJavaType(clazz);


        DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
        String gStr = DatasetDescriptionUtils.getSingleDefaultGraphUri(datasetDescription);
        if(gStr == null) {
            throw new RuntimeException("No target graph specified");
        }
        Node g = NodeFactory.createURI(gStr);

        DatasetGraph newState = rdfClass.createDatasetGraph(entity, g);

        System.out.println("oldState");
        DatasetGraphUtils.write(System.out, oldState);

        System.out.println("newState");
        DatasetGraphUtils.write(System.out, newState);

        Diff<Set<Quad>> diff = UpdateDiffUtils.computeDelta(newState, oldState);
        System.out.println("diff: " + diff);
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
        UpdateExecutionUtils.executeUpdate(uef, diff);

        return entity;

//        UpdateExecutionUtils.executeUpdateDelta(uef, newState, oldState);
    }


    @Override
    public void remove(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey,
            Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey,
            LockModeType lockMode, Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
    }

    @Override
    public FlushModeType getFlushMode() {
        return null;
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
    }

    @Override
    public void lock(Object entity, LockModeType lockMode,
            Map<String, Object> properties) {
    }

    @Override
    public void refresh(Object entity) {
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void detach(Object entity) {
    }

    @Override
    public boolean contains(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return LockModeType.NONE;
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createQuery(String qlString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createNamedQuery(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void joinTransaction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return null;
    }

    @Override
    public Object getDelegate() {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public EntityTransaction getTransaction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaBuilderJena getCriteriaBuilder() {
        CriteriaBuilderJena result = new CriteriaBuilderJena();
        return result;
    }

    @Override
    public Metamodel getMetamodel() {
        throw new UnsupportedOperationException();
    }


}
