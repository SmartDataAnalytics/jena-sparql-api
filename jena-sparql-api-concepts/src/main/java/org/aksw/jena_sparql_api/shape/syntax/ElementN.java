package org.aksw.jena_sparql_api.shape.syntax;

import java.util.ArrayList;
import java.util.List;

public abstract class ElementN
    implements Element
{
    protected List<Element> members;

    public ElementN() {
        members = new ArrayList<Element>();
    }

    public List<Element> getMembers() {
        return members;
    }
}
