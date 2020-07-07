package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.mapper.descriptor.impl.SimpleType;

import com.google.common.collect.Table;


public class ClassDescriptor {
    // iri to type to methods
    Map<String, Table<SimpleType, String, MethodGroup>> iriToNameToGroup;//  = new LinkedHashMap<>();

    // A collection where order does not matter
    ClassDescriptor registerSet(String iri) {
        return null;
    }

    // A collection where order matterns
    ClassDescriptor registerList(String iri) {
        return null;
    }

    HashIdProcessor getHashIdProcessor() {
        return null;
    }

    public List<PropertyDescriptor> getPropertyDescs() {
        return null;
    }
}

