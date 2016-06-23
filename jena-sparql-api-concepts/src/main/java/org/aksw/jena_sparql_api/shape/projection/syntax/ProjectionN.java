package org.aksw.jena_sparql_api.shape.projection.syntax;

import java.util.List;

public abstract class ProjectionN
    implements Projection
{
    protected List<Projection> members;

    public ProjectionN(List<Projection> members) {
        this.members = members;
    }

    public List<Projection> getMembers() {
        return this.members;
    }
}
