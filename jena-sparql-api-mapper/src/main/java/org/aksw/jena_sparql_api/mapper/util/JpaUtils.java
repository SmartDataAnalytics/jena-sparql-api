package org.aksw.jena_sparql_api.mapper.util;

import java.util.List;
import java.util.function.BiConsumer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * A few utility functions to reduce the boiler plate
 *
 * @author raven
 *
 */
public class JpaUtils {

    public static <T> TypedQuery<T> createTypedQuery(EntityManager em, Class<T> clazz, BiConsumer<CriteriaBuilder, CriteriaQuery<T>> fn) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);

        fn.accept(cb, cq);

        TypedQuery<T> result = em.createQuery(cq);
        return result;
    }

    public static <T> T getSingleResult(EntityManager em, Class<T> clazz, BiConsumer<CriteriaBuilder, CriteriaQuery<T>> fn) {
        TypedQuery<T> query = createTypedQuery(em, clazz, fn);
        T result = query.getSingleResult();
        return result;
    }

    public static <T> List<T> getResultList(EntityManager em, Class<T> clazz, BiConsumer<CriteriaBuilder, CriteriaQuery<T>> fn) {
        TypedQuery<T> query = createTypedQuery(em, clazz, fn);
        List<T> result = query.getResultList();
        return result;
    }

}
