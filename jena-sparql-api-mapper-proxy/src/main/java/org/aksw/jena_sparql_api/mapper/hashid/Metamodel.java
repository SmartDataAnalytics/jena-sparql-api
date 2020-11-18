package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


public class Metamodel {

    private static Metamodel INSTANCE = null;

    public static Metamodel get() {
        if(INSTANCE == null) {
            synchronized (Metamodel.class) {
                if(INSTANCE == null) {
                    INSTANCE = new Metamodel();
                }
            }
        }

        return INSTANCE;
    }

    protected Map<Class<?>, ClassDescriptor> classToDescriptor = new LinkedHashMap<>();
    protected BiMap<Class<?>, Class<?>> originalClassToProxyClass = HashBiMap.create();


    public static class PropertyDescCollection {
        public boolean doesOrderMatter() {
            return false;
        }
        public Collection<Object> getValue(RDFNode noed) {
//            Collection<? extends A> z = this.test();
//            A x = null;
//            z.add(x);

//            Collection<? extends A> x;
//            Set<B> y = null;
//            x = y;
//            x.add(foo);
            return null;
        }
    }

    public Class<?> getOriginalClass(Class<?> proxyClass) {
        Class<?> result = originalClassToProxyClass.inverse().get(proxyClass);
        return result;
    }


    public void registerProxyClass(Class<?> originalClass, Class<?> proxyClass) {
        originalClassToProxyClass.put(originalClass, proxyClass);
    }

    public ClassDescriptor get(Class<?> clazz) {
        ClassDescriptor result = classToDescriptor.get(clazz);
        return result;
    }

    public ClassDescriptor getOrCreate(Class<?> clazz) {
        ClassDescriptor result = classToDescriptor.computeIfAbsent(clazz, c -> new ClassDescriptor(clazz));
        return result;
    }
}
