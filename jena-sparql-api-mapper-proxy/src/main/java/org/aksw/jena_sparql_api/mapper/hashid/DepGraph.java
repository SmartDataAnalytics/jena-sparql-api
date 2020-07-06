package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

public class DepGraph {
    protected ClassToInstanceMap<ClassDesc> classToDesc = MutableClassToInstanceMap.create();

    public static class MemberDesc {
        String iri;
    }

    public static class PropertyDesc {
        String iri;

        public String getIri() {
            return iri;
        }

        public boolean isInverse() {
            return false;
        }

        public boolean isCollection() {
            return false;
        }

        public boolean isHashId() {
            return false;
        }

        public Object getValue(RDFNode node) {
            return null;
        }

        public PropertyDescCollection asCollectionProperty() {
            return null;
        }
    }

    public static class PropertyDescCollection {
        public boolean doesOrderMatter() {
            return false;
        }

        public Collection<Object> getValue(RDFNode noed) {
            return null;
        }
    }


    public static class ClassDesc {
        Map<String, MemberDesc> iriToMember = new LinkedHashMap<>();

        // A collection where order does not matter
        ClassDesc registerSet(String iri) {
            return null;
        }

        // A collection where order matterns
        ClassDesc registerList(String iri) {
            return null;
        }


        public List<PropertyDesc> getPropertyDescs() {
            return null;
        }
    }



    public ClassDesc getOrCreate(Class<?> clazz) {
//        ClassDesc result = classToDesc.computeIfAbsent(clazz, c -> new ClassDesc());
//        return result;
        return null;
    }
}
