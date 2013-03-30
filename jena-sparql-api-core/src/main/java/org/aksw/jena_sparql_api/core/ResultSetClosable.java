package org.aksw.jena_sparql_api.core;

import org.aksw.commons.collections.IClosable;

import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/7/12
 *         Time: 1:07 AM
 */
public class ResultSetClosable
    extends ResultSetClose
{
    private IClosable closable;

    public ResultSetClosable(ResultSet decoratee) {
        super(decoratee, true);
        this.closable = null;
    }

    public ResultSetClosable(ResultSet decoratee, IClosable closable) {
        super(decoratee, true);
        if(closable == null) {
            throw new NullPointerException();
        }
        this.closable = closable;

        super.checkClose();
    }

    @Override
    public void close() {
    	if(closable != null) {
    		closable.close();
    	}
    }
}
