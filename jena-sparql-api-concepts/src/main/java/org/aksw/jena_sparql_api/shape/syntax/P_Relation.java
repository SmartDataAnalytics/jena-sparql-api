package org.aksw.jena_sparql_api.shape.syntax;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * Path which makes use of an arbitrary SPARQL relation
 *
 * @author raven
 *
 */
public class P_Relation
    extends PathBaseEx
{
    protected Relation relation;

    @Override
    public void visit(PathExVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((relation == null) ? 0 : relation.hashCode());
        return result;
    }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap) {
        boolean result;

        // TODO We should make use of the isoMap
        if(path2 instanceof P_Relation) {
            P_Relation p = (P_Relation)path2;
            result = relation == null ? p.relation == null : relation.equals(p.relation);
        } else {
            result = false;
        }

        return result;
    }

}
