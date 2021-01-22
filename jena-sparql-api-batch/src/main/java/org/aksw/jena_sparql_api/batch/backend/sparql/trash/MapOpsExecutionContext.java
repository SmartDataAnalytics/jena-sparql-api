package org.aksw.jena_sparql_api.batch.backend.sparql.trash;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.beans.model.MapOpsBase;
import org.springframework.batch.item.ExecutionContext;


public class MapOpsExecutionContext
    extends MapOpsBase<ExecutionContext, String, Object>
{

    public MapOpsExecutionContext() {
        super(ExecutionContext.class, String.class, Object.class);
    }


    @Override
    public boolean $containsKey(ExecutionContext entity, Object key) {        
        boolean result = key instanceof String ? entity.containsKey((String)key) : false;
        return result;
    }

    @Override
    public Object $get(ExecutionContext entity, Object key) {
        Object result = key instanceof String ? entity.get((String)key) : null;
        return result;
    }

    @Override
    public void $remove(ExecutionContext entity, Object key) {
        entity.remove((String)key);
    }

//    @Override
//    public void $clear(ExecutionContext entity) {
//        
//        
//    }

    @Override
    public void $put(ExecutionContext entity, String key,
            Object value) {
        entity.put((String)key, value);
    }

    @Override
    public Set<String> $keySet(ExecutionContext entity) {
        Set<String> result = entity.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toSet());
        return result;
    }

    @Override
    public int $size(ExecutionContext entity) {
        int result = entity.size();
        return result;
    }

    @Override
    public void $clear(ExecutionContext entity) {
        keySet(entity).forEach(key -> $remove(entity, key));
    }
    
    @Override
    public Set<? extends Entry<? super String, ? super Object>> $entrySet(
            ExecutionContext entity) {
        return entity.entrySet();
    }

}
