package org.aksw.jena_sparql_api.update_old;

import org.apache.jena.sparql.core.Quad;

interface UpdateListenerOld {
    void onPreBatchStart();

    void onPreInsert(Quad quad);
    void onPreDelete(Quad quad);

    void onPreBatchEnd();


    void onPostBatchStart();

    void onPostInsert(Quad quad);
    void onPostDelete(Quad quad);

    void onPostBatchEnd();
}