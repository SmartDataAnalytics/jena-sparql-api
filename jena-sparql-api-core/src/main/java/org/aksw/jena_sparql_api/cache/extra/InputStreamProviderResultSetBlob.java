package org.aksw.jena_sparql_api.cache.extra;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:54 PM
 */
@Deprecated
public class InputStreamProviderResultSetBlob
    implements InputStreamProvider
{
    private java.sql.ResultSet rs;
    private Blob blob;

    public InputStreamProviderResultSetBlob(java.sql.ResultSet rs, Blob blob) {
        this.rs = rs;
        this.blob = blob;
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
        SqlUtils.close(rs);
    }
}
