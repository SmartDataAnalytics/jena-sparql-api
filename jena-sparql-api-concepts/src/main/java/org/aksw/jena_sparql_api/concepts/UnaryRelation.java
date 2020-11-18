package org.aksw.jena_sparql_api.concepts;

import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public interface UnaryRelation
    extends Relation
{
    Var getVar();

    @Override
    default List<Var> getVars() {
        Var v = getVar();
        return Collections.singletonList(v);
    }


    /**
     * Test whether this relation is isomorphic to
     * {@code ?s WHERE { ?s ?p ?o }}
     *
     * @return
     */
    default boolean isSubjectConcept() {
        return ConceptUtils.isSubjectConcept(this);
    }

    default Query asQuery() {
        Element e = getElement();
        List<Var> vs = getVars();

        Query result = new Query();
        result.setQuerySelectType();

        result.setQueryPattern(e);
        result.setDistinct(true);

        for(Var v : vs) {
            result.getProjectVars().add(v);
        }

        return result;
    }

}
