package org.aksw.jena_sparql_api.update;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.CacheSet;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.jena.sparql.core.Quad;


/**
 * A DatasetListener that only forwards addition/delete events if these actions
 * actually need to be carried out.
 *
 * @author raven
 *
 */
public class GraphListererUnique
    implements DatasetListener
{
    private Set<DatasetListener> graphListeners = new HashSet<DatasetListener>();

    private Set<Quad> insertCache = new CacheSet<Quad>(10000, true);
    private Set<Quad> deleteCache = new CacheSet<Quad>(10000, true);

    private Set<Quad> safeInserts = new HashSet<Quad>();
    private Set<Quad> safeDeletes = new HashSet<Quad>();

    private Set<Quad> verifyInserts = new HashSet<Quad>();
    private Set<Quad> verifyDeletes = new HashSet<Quad>();


    @Override
    public void onPreDelete(Iterator<Quad> it) {

    }

    @Override
    public void onPreInsert(Iterator<Quad> it) {
        int batchSize = 10;

        Iterator<Quad> chunkIt = it;
        while(chunkIt.hasNext()) {
            chunkIt = Iterators.limit(it, batchSize);
            Set<Quad> quads = Sets.newHashSet(chunkIt);



        }

    }


    public Set<Quad> verify(Collection<Quad> quads, boolean shouldExist) {
        Set<Quad> result = new HashSet<Quad>();

        for(Quad quad : quads) {
            String askQuery = FilterCompiler.askForQuad(quad);
            System.out.println(askQuery);
            boolean exists = endpoint.createQueryExecution(askQuery).execAsk();

            /*
             TODO Can we set the caches here? I guess not
            if(exists) {
                insertCache.add(quad);
            } else {
                deleteCache.add(quad);
            }*/

            if(exists == shouldExist) {
                result.add(quad);
            }
        }

        return result;
    }

    /*
    public void doInsert() {
        for(GraphListener listener : graphListeners) {
            listener.onPreBatchStart();

            for(Quad quad : safeInserts) {
                listener.onPreInsert(quad);
            }

            listener.onPreBatchEnd();
        }

        safeInserts.clear();
    }

    public void doDelete() {
        for(GraphListener listener : graphListeners) {
            listener.onPreBatchStart();

            for(Quad quad : safeDeletes) {
                listener.onPreDelete(quad);
            }

            listener.onPreBatchEnd();
        }

        safeDeletes.clear();
    }
    */

    public void verifyInserts() {
        Set<Quad> verified = verify(verifyInserts, false);
        safeInserts.addAll(verified);
        verifyInserts.clear();
    }

    public void verifyDeletes() {
        Set<Quad> verified = verify(verifyDeletes, true);
        safeDeletes.addAll(verified);
        verifyDeletes.clear();
    }


    @Override
    public void onPreInsert(Quad quad) {
        if(insertCache.contains(quad)) {
            return;
        }

        if(deleteCache.contains(quad)) {
            safeInserts.add(quad);
            deleteCache.remove(quad);
            insertCache.add(quad);
        } else {
            verifyInserts.add(quad);
        }
    }

    @Override
    public void onPreDelete(Quad quad) {
        if(deleteCache.contains(quad)) {
            return;
        }

        if(insertCache.contains(quad)) {
            safeDeletes.add(quad);
            insertCache.remove(quad);
            deleteCache.add(quad);
        } else {
            verifyDeletes.add(quad);
        }
    }


    public Set<UpdateListenerOld> getGraphListeners()
    {
        return graphListeners;
    }

    @Override
    public void onPreBatchStart() {
    }

    @Override
    public void onPreBatchEnd() {
        verifyDeletes();
        verifyInserts();

        for(UpdateListenerOld listener : graphListeners) {
            listener.onPreBatchStart();

            for(Quad item : safeDeletes) {
                listener.onPreDelete(item);
            }

            for(Quad item : safeInserts) {
                listener.onPreInsert(item);
            }

            listener.onPreBatchEnd();
        }
    }

    @Override
    public void onPostBatchEnd() {
        for(UpdateListenerOld listener : graphListeners) {
            listener.onPostBatchStart();

            for(Quad item : safeDeletes) {
                listener.onPostDelete(item);
            }

            for(Quad item : safeInserts) {
                listener.onPostInsert(item);
            }

            listener.onPostBatchEnd();
        }

        safeInserts.clear();
        safeDeletes.clear();
    }
}
