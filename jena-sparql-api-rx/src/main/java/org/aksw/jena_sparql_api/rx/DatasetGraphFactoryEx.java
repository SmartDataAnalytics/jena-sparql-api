package org.aksw.jena_sparql_api.rx;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.core.mem.QuadTable;
import org.apache.jena.sparql.core.mem.TripleTable;

public interface DatasetGraphFactoryEx {
    public static DatasetGraph createInsertOrderPreservingDatasetGraph() {
        QuadTable quadTable = new QuadTableFromNestedMaps();
        TripleTable tripleTable = new TripleTableFromQuadTable(quadTable);
        DatasetGraph result = new DatasetGraphInMemory(quadTable, tripleTable);
        return result;
    }
}
