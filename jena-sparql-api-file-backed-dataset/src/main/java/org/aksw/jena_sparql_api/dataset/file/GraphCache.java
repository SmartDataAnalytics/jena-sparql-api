package org.aksw.jena_sparql_api.dataset.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutionException;

import org.apache.jena.ext.com.google.common.cache.Cache;
import org.apache.jena.sparql.core.DatasetGraph;

public class GraphCache {
    public static class State {
        public State(DatasetGraph cachedData, String version) {
            super();
            this.cachedData = cachedData;
            this.version = version;
        }

        public DatasetGraph cachedData;
        String version; // timestamp of the file
    }

    protected Cache<Path, State> cache;

    public DatasetGraph load(Path path) throws ExecutionException, IOException {
        FileTime time = Files.getLastModifiedTime(path);
        String actualVersion = time.toInstant().toString();

        State state = cache.get(path, () -> {
            DatasetGraph data = null;
            State newState = new State(data, actualVersion);
            return newState;
        });

        if(!actualVersion.equals(state.version)) {
            // Reload the data
        }

        DatasetGraph result = null;
        return result;
    }
}