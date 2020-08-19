package org.aksw.jena_sparql_api.cache.extra;


import java.util.Iterator;

import org.aksw.commons.collections.IClosable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 2:42 PM
 */
public interface CacheResource
    extends IClosable
{
    boolean isOutdated();

    // isCachingInProgress()
    // void abort();
    // isModel() isResultSet() isBoolean()

    //InputStream open();

    Model asModel(Model result);
    Iterator<Triple> asIteratorTriples();
    //Iterator<Quad> asIteratorQuads();

    ResultSet asResultSet();
    boolean asBoolean();
}
