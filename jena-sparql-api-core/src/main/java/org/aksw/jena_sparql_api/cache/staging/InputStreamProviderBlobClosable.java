package org.aksw.jena_sparql_api.cache.staging;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.aksw.commons.collections.IClosable;
import org.aksw.jena_sparql_api.cache.extra.InputStreamProvider;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:54 PM
 */
public class InputStreamProviderBlobClosable
    implements InputStreamProvider
{
    //private java.sql.ResultSet rs;
	private IClosable closable;
    private Blob blob;

    public InputStreamProviderBlobClosable(Blob blob, IClosable closable) {
        //this.rs = rs;
        this.blob = blob;
        this.closable = closable;
    }


    @Override
    public InputStream open() {
        try {
            return blob.getBinaryStream();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    	if(closable != null) {
    		closable.close();
    	}
        //SqlUtils.close(rs);
    }
}
