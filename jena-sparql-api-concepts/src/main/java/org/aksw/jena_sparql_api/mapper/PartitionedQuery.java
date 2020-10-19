package org.aksw.jena_sparql_api.mapper;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.HasElement;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;

public interface PartitionedQuery
    extends HasElement
{
    Query getQuery();
    List<Var> getPartitionVars();

    default Template getTemplate() {
        Query query = getQuery();
        Template result = query.getConstructTemplate();
        return result;
    }

    @Override
    default Element getElement() {
        Query query = getQuery();
        Element result = query.getQueryPattern();
        return result;
    }
}
