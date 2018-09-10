package org.aksw.jena_sparql_api.mapper.context;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * An object for associating information with beans
 *
 * @author raven
 *
 */
public class EntityContextImpl<T>
    implements EntityContext<T>
{
    protected Map<T, Map<String, Object>> entityStates;

    public EntityContextImpl() {
        this(new HashMap<T, Map<String, Object>>());
    }

    public EntityContextImpl(Map<T, Map<String, Object>> beanStates) {
        super();
        this.entityStates = beanStates;
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.mapper.context.EntityContext#get(java.lang.Object)
     */
    @Override
    public Map<String, Object> get(Object entity) {
        checkManaged(entity);

        Map<String, Object> result = entityStates.get(entity);
        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.mapper.context.EntityContext#getOrCreate(T)
     */
    @Override
    public Map<String, Object> getOrCreate(T entity) {
        Map<String, Object> result = isManaged(entity)
                ? get(entity)
                : register(entity)
                ;
        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.mapper.context.EntityContext#register(T)
     */
    @Override
    public Map<String, Object> register(T entity) {
        Map<String, Object> result = new HashMap<String, Object>();
        entityStates.put(entity, result);
        return result;
    }

    public void checkManaged(Object entity) {
        if(!isManaged(entity)) {
            throw new RuntimeException("Entity was expected to be managed: " + entity);
        }
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.mapper.context.EntityContext#getState(java.lang.Object)
     */
    @Override
    public Map<String, Object> getState(Object entity) {
        checkManaged(entity);

        Map<String, Object> result = entityStates.get(entity);
        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.jena_sparql_api.mapper.context.EntityContext#isManaged(java.lang.Object)
     */
    @Override
    public boolean isManaged(Object entity) {
        boolean result = entityStates.containsKey(entity);
        return result;
    }


    public static <T> EntityContext<T> createIdentityContext(Class<T> clazz) {
        Map<T, Map<String, Object>> map = new IdentityHashMap<T, Map<String, Object>>();

        EntityContext<T> result = new EntityContextImpl<T>(map);
        return result;
    }

    @Override
    public void setAttribute(T entity, String attribute, Object value) {
        Map<String, Object> map = getOrCreate(entity);
        map.put(attribute, value);
    }

    @Override
    public <X> X getAttribute(Object entity, String attribute, X defaultValue) {
        @SuppressWarnings("unchecked")
        X result = isManaged(entity)
            ? (X)getState(entity).get(attribute)
            : defaultValue
            ;

        return result;
    }
}
