package org.aksw.sparqlify.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PrefixIndexMetaFactory
    implements MetaIndexFactory
{
    public static Set<Class<?>> supportedConstraintClasses = new HashSet<Class<?>>();

    static
    {
        supportedConstraintClasses.add(PrefixConstraint.class);
    }

    public Set<Class<?>> getSupportedConstraintClasses() {
        return supportedConstraintClasses;
    }


    private Function<Object, Set<String>> prefixExtractor;

    public PrefixIndexMetaFactory(Function<Object, Set<String>> prefixExtractor) {
        this.prefixExtractor = prefixExtractor;
    }

    @Override
    public MapStoreAccessor create(Table table, List<String> columnNames) {

        int[] indexColumns = new int[columnNames.size()];

        for(int i = 0; i < indexColumns.length; ++i) {
            String columnName = columnNames.get(i);
            indexColumns[i] = table.getColumns().getIndex(columnName);
        }

        PrefixMapStoreAccessor accessor = new PrefixMapStoreAccessor(indexColumns, prefixExtractor);

        return accessor;
    }

}