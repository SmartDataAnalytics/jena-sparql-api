package org.aksw.jena_sparql_api.batch;

import org.apache.jena.sparql.engine.binding.Binding;

public class BindingMapperPassThrough
    implements BindingMapper<Binding>
{
    public Binding map(Binding binding, long rowNum) {
        return binding;
    }
}