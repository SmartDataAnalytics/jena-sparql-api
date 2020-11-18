package org.aksw.jena_sparql_api.shape.projection.syntax;

import java.util.List;

public class ProjectionGroup
    extends ProjectionN
{
    public ProjectionGroup(List<Projection> members) {
        super(members);
    }

    @Override
    public <T> T accept(ProjectionVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
