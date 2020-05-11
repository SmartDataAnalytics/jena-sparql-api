package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.rxjava3.core.Single;

/**
 * An InputStreamSupplier is a source for InputStreams
 * Obtaining an instance from the supplier should be near instant.
 * For example, the most typical form of an InputStreamSupplier is () -> Files.newInputStream(someFile);
 *
 * Conversely, the process should not trigger a workflow that has to prepare the data first.
 *
 * Workflows that prepare a source of InputStreams should
 * be realized using a Single<InputStreamSupplier>.
 *
 *
 * @author raven
 *
 */
@FunctionalInterface
public interface InputStreamSupplier {
    Single<InputStream> execStream() throws IOException;
}
