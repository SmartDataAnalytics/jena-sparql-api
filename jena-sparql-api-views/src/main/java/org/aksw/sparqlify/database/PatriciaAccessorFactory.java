package org.aksw.sparqlify.database;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PatriciaAccessorFactory
    implements MetaIndexFactory
{
    private Function<Object, Set<String>> prefixExtractor;


    public PatriciaAccessorFactory(Function<Object, Set<String>> prefixExtractor) {
        this.prefixExtractor = prefixExtractor;
    }


    @Override
    public MapStoreAccessor create(Table table, List<String> columnNames) {

        int[] indexColumns = new int[columnNames.size()];

        for(int i = 0; i < indexColumns.length; ++i) {
            String columnName = columnNames.get(i);
            indexColumns[i] = table.getColumns().getIndex(columnName);
        }

        MapStoreAccessor accessor = null;
        if(true) {
            throw new RuntimeException("PatriciaTree support disabled");
        //PatriciaPrefixMapStoreAccessor accessor = new PatriciaPrefixMapStoreAccessor(indexColumns, prefixExtractor);
        }

        return accessor;
    }

}