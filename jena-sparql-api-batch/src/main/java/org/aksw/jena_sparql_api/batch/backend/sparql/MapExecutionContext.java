package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.item.ExecutionContext;

public class MapExecutionContext
    extends AbstractMap<String, Object>
{
    protected ExecutionContext ctx;
    
    public MapExecutionContext(ExecutionContext ctx) {
        super();
        this.ctx = ctx;
    }

    @Override
    public Object get(Object key) {
        Object result = key instanceof String ? ctx.get((String)key) : null;
        return result;
    }

    @Override
    public Object put(String key, Object value) {
        ctx.put(key, value);
        return value;
    }
    
    @Override
    public boolean containsKey(Object key) {
        boolean result = key instanceof String ? ctx.containsKey((String)key) : null;
        return result;
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return ctx.entrySet();
    }
    
    @SuppressWarnings("rawtypes")
    public static Map createMapView(Object o) {
        Map result = new MapExecutionContext((ExecutionContext)o);
        return result;        
    }
}
