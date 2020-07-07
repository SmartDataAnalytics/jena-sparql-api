package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

interface A {
}

interface B extends A { }

public class DepGraph {
    protected ClassToInstanceMap<ClassDescriptor> classToDesc = MutableClassToInstanceMap.create();


    public static class PropertyDescCollection {
        public boolean doesOrderMatter() {
            return false;
        }

        Collection<? extends A> test(){  return null; }

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


    public ClassDescriptor getOrCreate(Class<?> clazz) {
//        ClassDesc result = classToDesc.computeIfAbsent(clazz, c -> new ClassDesc());
//        return result;
        return null;
    }
}
