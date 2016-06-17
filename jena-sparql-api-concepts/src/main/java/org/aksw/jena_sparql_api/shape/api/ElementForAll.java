package org.aksw.jena_sparql_api.shape.api;

import org.apache.jena.sparql.path.Path;

public class ElementForAll
    extends ElementPathConstraint
{
    public ElementForAll(Path path, Element filler) {
        super(path, filler);
    }
}
