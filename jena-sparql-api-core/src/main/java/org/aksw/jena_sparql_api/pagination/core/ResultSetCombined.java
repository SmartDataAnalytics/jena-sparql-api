package org.aksw.jena_sparql_api.pagination.core;

/**
 * Not used
 *
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 11:56 PM
 * /
public class ResultSetCombined
    implements ResultSet
{
    private Iterator<ResultSet> it;
    private ResultSet current = null;

    public ResultSetCombined(Iterator<ResultSet> it) {
        this.it = it;
    }

    private boolean checkCurrent() {
        System.out.println("Checking current");
        while(current == null) {
            if(!it.hasNext()) {
                return false;
            }
            current = it.next();

            if(!current.hasNext()) {
                continue;
            }
        }
        return true;
    }

    public QuerySolution next() {
        return checkCurrent() ? current.next() : null;
    }

    @Override
    public void remove() {
        //return current.remove();
    }

    public Binding nextBinding() {
        return checkCurrent() ? current.nextBinding() : null;
    }

    @Override
    public boolean hasNext() {
        return checkCurrent();
        // TODO something goes wrong here
        //return it.hasNext() || current != null && current.hasNext();
    }

    @Override
    public QuerySolution nextSolution() {
        return checkCurrent() ? current.nextSolution() : null;
    }

    @Override
    public int getRowNumber() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getResultVars() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Model getResourceModel() {
        return checkCurrent() ? current.getResourceModel() : null;
    }


}
*/