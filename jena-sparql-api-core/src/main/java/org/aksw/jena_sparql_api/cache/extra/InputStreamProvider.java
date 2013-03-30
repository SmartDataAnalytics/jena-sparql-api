package org.aksw.jena_sparql_api.cache.extra;

import java.io.InputStream;

import org.aksw.commons.collections.IClosable;

/**
 * A class that can provide an InputStream.
 * Note: The provide itself might require closing:
 *
 * The use case is to have a resultset, that offers a clob, that offers an InputStream.
 * on the one hand, the inputstream to the clob has to be closed, on the other
 * hand the result set needs to be closed.
 *
 *
 *
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 10:25 PM
 */
public interface InputStreamProvider
    extends IClosable
{
    InputStream open();
}
