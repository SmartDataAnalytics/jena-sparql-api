package org.aksw.jena_sparql_api.concepts;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.syntax.Element;

public class RelationUtils {
    public static Query createQuery(Relation relation) {
        Query result = new Query();
        result.setQuerySelectType();

        Element e = relation.getElement();;
        result.setQueryPattern(e);

        VarExprList project = result.getProject();
        project.add(relation.getSourceVar());
        project.add(relation.getTargetVar());

        return result;
    }
}
