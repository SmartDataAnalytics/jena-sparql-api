package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * An object that contains information about how to resolve the property values
 * of an entity
 *
 *
 * @author raven
 *
 */
public class EntityFragment {
    protected Object rootEntity;
    protected Map<Object, Map<String, PlaceholderInfo>> propertyInfos;
    protected List<ResolutionTask<PlaceholderInfo>> populationTasks;


    public EntityFragment(Object rootEntity) {
        this.rootEntity = rootEntity;
        this.propertyInfos = new IdentityHashMap<>();
        this.populationTasks = new ArrayList<>();
    }

    public Map<Object, Map<String, PlaceholderInfo>> getPropertyInfos() {
        return propertyInfos;
    }

    public Object getRootEntity() {
        return rootEntity;
    }

    public List<ResolutionTask<PlaceholderInfo>> getTasks() {
        return populationTasks;
    }
}
