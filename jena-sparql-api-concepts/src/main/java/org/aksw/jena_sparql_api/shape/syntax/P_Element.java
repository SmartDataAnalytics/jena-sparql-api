package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class P_Element
    extends PathBaseEx
{
    protected Element element;

    public P_Element(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public void visit(PathExVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap) {
        // TODO Auto-generated method stub
        return false;
    }

}
