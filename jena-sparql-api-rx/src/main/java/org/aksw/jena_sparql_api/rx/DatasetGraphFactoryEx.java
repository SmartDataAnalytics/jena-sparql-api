package org.aksw.jena_sparql_api.rx;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.dboe.QuadTableCore;
import org.aksw.jena_sparql_api.dboe.QuadTableCoreFromMapOfTripleTable;
import org.aksw.jena_sparql_api.dboe.QuadTableWithInsertOrderPreservation;
import org.aksw.jena_sparql_api.dboe.StorageRDFBasic;
import org.aksw.jena_sparql_api.dboe.TripleTableCore;
import org.aksw.jena_sparql_api.dboe.TripleTableCoreFromNestedMapsImpl;
import org.aksw.jena_sparql_api.dboe.TripleTableWithInsertOrderPreservation;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.dboe.storage.simple.StoragePrefixesSimpleMem;
import org.apache.jena.dboe.storage.system.DatasetGraphStorage;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.TransactionalLock;

public interface DatasetGraphFactoryEx {
    public static DatasetGraph createInsertOrderPreservingDatasetGraph() {
        return createInsertOrderPreservingDatasetGraph(false, false);
    }

    public static DatasetGraph createInsertOrderPreservingDatasetGraph(boolean strictOrderOnQuads, boolean strictOrderOnTriples) {
        Supplier<TripleTableCore> tripleTableSupplier = strictOrderOnTriples
                ? () -> new TripleTableWithInsertOrderPreservation(new TripleTableCoreFromNestedMapsImpl())
                : () -> new TripleTableCoreFromNestedMapsImpl();

        QuadTableCore quadTable = new QuadTableCoreFromMapOfTripleTable(tripleTableSupplier);

        if (strictOrderOnQuads) {
            quadTable = new QuadTableWithInsertOrderPreservation(quadTable);
        }

        StorageRDF storage = StorageRDFBasic.createWithQuadsOnly(quadTable);
        DatasetGraph result = new DatasetGraphStorage(storage, new StoragePrefixesSimpleMem(), TransactionalLock.createMRSW());
        return result;
    }
}
