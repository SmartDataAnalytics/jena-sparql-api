package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.List;

public abstract class ResolutionTaskBase<T>
    implements ResolutionTask<T>
{
    protected List<T> placeholders;

    public ResolutionTaskBase(List<T> placeholders) {
        super();
        this.placeholders = placeholders;
    }

    @Override
    public List<T> getPlaceholders() {
        return placeholders;
    }
}
