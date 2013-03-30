package org.aksw.jena_sparql_api.cache.extra;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.commons.collections.IClosable;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:54 PM
 */
public class ClosableCacheSql
    implements IClosable
{
    private CacheResource resource;
    private InputStream in;

    public ClosableCacheSql(CacheResource resource, InputStream in) {
        this.resource = resource;
        this.in = in;
    }


    @Override
    public void close() {
        //SqlUtils.close(rs);
        resource.close();
        if(in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
