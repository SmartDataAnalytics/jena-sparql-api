package org.aksw.jena_sparql_api.mapper.jpa.core;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngine;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.CriteriaBuilderJena;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;


public class EntityManagerJena
    implements EntityManager
{
	protected RdfMapperEngine engine;

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


    public EntityManagerJena(RdfMapperEngine engine) {
        super();
        this.engine = engine;
    }

//    public RdfPersistenceContext getPersistenceContext() {
//    	RdfPersistenceContext result = engine instanceof PersistenceContextSupplier
//			? ((PersistenceContextSupplier)engine).getPersistenceContext()
//			: null
//			;
//
//		return result;
//    }


    public RdfTypeFactory getRdfTypeFactory() {
    	RdfTypeFactory result = engine.getRdfTypeFactory();
    	return result;
    }


    @Override
    public <T> T find(Class<T> clazz, Object primaryKey) {

    	//LookupService<Node, T> ls = engine.getLookupService(clazz);

        Node node;
        if(primaryKey instanceof String) {
            node = NodeFactory.createURI((String)primaryKey);
        } else if(primaryKey instanceof Node) {
            node = (Node)primaryKey;
        } else {
            throw new RuntimeException("Invalid primary key type: " + primaryKey);
        }

        T result = engine.find(clazz, node);
        //Map<Node, T> nodeToBean = ls.apply(Collections.singleton(node));
        //T result = nodeToBean.get(node);

        return result;
    }

    @Override
    public void persist(Object entity) {
        merge(entity);
    }

    @Override
    public <T> T merge(T entity) {
    	T result = engine.merge(entity);
    	return result;
    }


    @Override
    public void remove(Object entity) {
    	engine.remove(entity);
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
