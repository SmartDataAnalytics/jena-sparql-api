package org.aksw.jena_sparql_api.shape.syntax;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.Path;

public class RelationPattern {
    protected Path path; // Note: we could / should extend on the standard path expressions
    protected Var targetVar; // Optional target variable name
    protected SparqlServiceReference service; // Optional service from which to resolve the relation assertions
    protected Element filterPattern;
}
