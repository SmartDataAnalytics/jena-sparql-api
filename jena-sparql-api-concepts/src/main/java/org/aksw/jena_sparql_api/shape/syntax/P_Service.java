package org.aksw.jena_sparql_api.shape.syntax;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class P_Service
    extends PathBaseEx
{
    protected SparqlServiceReference service;

    public P_Service(SparqlServiceReference service) {
        super();
        this.service = service;
    }

    public SparqlServiceReference getService() {
        return service;
    }

    @Override
    public void visit(PathVisitorEx visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap) {
        // TODO Auto-generated method stub
        return false;
    }
}
