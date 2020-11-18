package org.aksw.jena_sparql_api.shape.syntax;

import java.util.List;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Prologue;

public class ShapeQuery {
    protected Prologue prologue;
    protected ShapePattern shapePattern;
    protected Element conceptPattern;
    protected List<SortCondition> sortConditions;
    protected long limit;
    protected long offset;
}
