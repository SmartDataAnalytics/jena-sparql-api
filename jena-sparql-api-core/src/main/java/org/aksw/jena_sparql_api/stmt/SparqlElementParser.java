package org.aksw.jena_sparql_api.stmt;

import com.google.common.base.Function;
import org.apache.jena.sparql.syntax.Element;

public interface SparqlElementParser
    extends Function<String, Element>
{

}
