package org.aksw.jena_sparql_api.gson;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ExclusionStrategyClassAndFields implements ExclusionStrategy {

    private Multimap<Class<?>, String> classToFieldName = HashMultimap.create();
    private boolean whitelist;

    public ExclusionStrategyClassAndFields(Multimap<Class<?>, String> classToFieldName) {
        this(classToFieldName, false);
    }

    /**
     * 
     * @param classToFieldName
     *            The fields to be excluded
     * @param whitelist
     *            Skip the fields for which there is NO entry in the
     *            classToFieldName map
     */
    public ExclusionStrategyClassAndFields(
            Multimap<Class<?>, String> classToFieldName, boolean whitelist) {
        this.classToFieldName = classToFieldName;
        this.whitelist = whitelist;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        String fieldName = f.getName();
        Class<?> fieldClass = f.getDeclaringClass();

        Map<Class<?>, Collection<String>> map = classToFieldName.asMap();

        boolean result = false;

        for (Entry<Class<?>, Collection<String>> entry : map.entrySet()) {

            Class<?> entryClass = entry.getKey();
            Collection<String> entryFields = entry.getValue();

            if (entryClass.isAssignableFrom(fieldClass)) {
                if (entryFields.contains(fieldName)) {
                    result = true;
                    break;
                }
            }
        }

        // Negate the result if in whitelist mode
        if (whitelist) {
            result = !result;
        }

        return result;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
