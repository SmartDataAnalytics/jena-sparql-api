package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.Path;

public abstract class ElementPathConstraint
    extends Element0
{
    protected Path path;
    protected Element filler;

    public ElementPathConstraint(Path path, Element filler) {
        super();
        this.path = path;
        this.filler = filler;
    }

    public Path getPath() {
        return path;
    }

    public Element getFiller() {
        return filler;
    }

}
