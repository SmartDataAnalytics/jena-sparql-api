package fr.inrialpes.tyrexmo.testqc;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;

public abstract class LegacyContainmentSolverBase
    implements SimpleContainmentSolver, LegacyContainmentSolver
{
    @Override
    public boolean entailed(String queryStr1, String queryStr2) {
        Query q1 = QueryFactory.create(queryStr1);
        Query q2 = QueryFactory.create(queryStr2);
        boolean result;
        try {
            result = entailed(q1, q2);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public boolean entailedUnderSchema(String schema, String queryStr1, String queryStr2) {
        Query q1 = QueryFactory.create(queryStr1);
        Query q2 = QueryFactory.create(queryStr2);
        boolean result;
        try {
            result = entailedUnderSchema(schema, q1, q2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
