package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.concept_cache.core.SparqlQueryContainmentUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;

import fr.inrialpes.tyrexmo.testqc.ContainmentSolver;
import fr.inrialpes.tyrexmo.testqc.ContainmentTestException;
import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;

public abstract class ContainmentSolverWrapperBase
    implements ContainmentSolver, SimpleContainmentSolver
{
    @Override
    public void warmup() throws ContainmentTestException {
        SparqlQueryContainmentUtils.tryMatch(
            String.join("\n",
                "?x <my://type> <my://Airport> .",
                "?x <my://label> ?n ; ?h ?i . ",
                "FILTER(langMatches(lang(?n), 'en')) .",
                "FILTER(<mp://fn>(?x, ?n))"),

            String.join("\n",
                "?s <my://type> <my://Airport> .",
                "?s ?p ?l .",
                "FILTER(?p = <my://label> || ?p = <my://name>)")
        );
    }

    @Override
    public void cleanup() throws ContainmentTestException {
        System.gc();
    }

    @Override
    public boolean entailedUnderSchema(String schema, Query q1, Query q2) throws ContainmentTestException {
        throw new ContainmentTestException("Cannot yet parse Jena Models");
    }

    @Override
    public boolean entailedUnderSchema(Model schema, Query q1, Query q2) throws ContainmentTestException {
        throw new ContainmentTestException("Cannot yet parse Jena Models");
    }

    @Override
    public boolean entailed(String queryStr1, String queryStr2) {
        Query q1 = QueryFactory.create(queryStr1);
        Query q2 = QueryFactory.create(queryStr2);
        boolean result = entailed(q1, q2);
        return result;
    }

    @Override
    public boolean entailedUnderSchema(String schema, String queryStr1, String queryStr2) {
        Query q1 = QueryFactory.create(queryStr1);
        Query q2 = QueryFactory.create(queryStr2);
        boolean result;
        try {
            result = entailedUnderSchema(schema ,q1, q2);
        } catch (ContainmentTestException e) {
            throw new RuntimeException(e);
        }
        return result;

    }

}
