package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.Collection;
import java.util.List;

public interface ResolutionTask<T> {
    // The affected entity being populated
    //protected Object entity;
    //protected List<PlaceholderInfo> placeholders;

    /**
     * The root entity being affected
     *
     * @return
     */
    //Object getEntity();

    /**
     * The list of items that need to be resolved.
     *
     * @return
     */
    List<T> getPlaceholders();

    /**
     * A function that puts the resolved values into the entity
     */
    //protected Consumer<List<Object>> populator;

    /**
     * Populates the given entity with the given data.
     * May returns yet another set of tasks that require further resolution
     *
     * @param resolutions
     * @return
     */
    Collection<? extends ResolutionTask<T>> resolve(List<Object> resolutions);
}
