package org.aksw.jena_sparql_api.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/7/12 Time: 1:07 AM
 */
public class ResultSetCloseable extends ResultSetClose {
    private Closeable closeable;

    public ResultSetCloseable(ResultSet decoratee) {
        super(decoratee, true);
        this.closeable = null;
    }

    public ResultSetCloseable(ResultSet decoratee, Closeable closeable) {
        super(decoratee, true);
        if (closeable == null) {
            throw new NullPointerException();
        }
        this.closeable = closeable;

        super.checkClose();
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    public static ResultSetCloseable fromXml(InputStream xmlInputStream) {
        ResultSet rs = ResultSetFactory.fromXML(xmlInputStream);
        ResultSetCloseable result = new ResultSetCloseable(rs, xmlInputStream);
        return result;
    }
}
