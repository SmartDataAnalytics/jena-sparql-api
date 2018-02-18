package fr.inrialpes.tyrexmo.testqc;

import org.aksw.jena_sparql_api.query_containment.core.SparqlQueryContainmentUtils;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Resource;

public class SparqlQcPreparation {

    public static TaskImpl prepareTaskJena2(TestCase testCase, LegacyContainmentSolver solver) {

//        Query _viewQuery = SparqlQueryContainmentUtils.queryParser.apply("" + testCase.getSource());
//        Query _userQuery = SparqlQueryContainmentUtils.queryParser.apply("" + testCase.getTarget());

        com.hp.hpl.jena.query.Query viewQuery = com.hp.hpl.jena.query.QueryFactory.create(testCase.getSource().toString());
        com.hp.hpl.jena.query.Query userQuery = com.hp.hpl.jena.query.QueryFactory.create(testCase.getTarget().toString());

        return new TaskImpl(testCase,
            () -> {
            try {
                boolean actual = solver.entailed(viewQuery, userQuery);
                //String str = actual == testCase.getExpectedResult() ? "CORRECT" : "WRONG";
                return actual;
            } catch (ContainmentTestException e) {
                throw new RuntimeException(e);
            }
        }, () -> {
            try {
                solver.cleanup();
            } catch (ContainmentTestException e) {
                throw new RuntimeException();
            }
        });
    }

    public static TestCase parseTestCase(Resource t, boolean invertExpected) {
        String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        boolean expected = Boolean
                .parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

        expected = invertExpected ? !expected : expected;

        Query viewQuery = QueryFactory.create(srcQueryStr); //SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);
        Query userQuery = QueryFactory.create(tgtQueryStr); //SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);

        TestCase result = new TestCase(viewQuery, userQuery, expected);
        return result;
    }


    public static TaskImpl prepareTask(Resource w, Object o, boolean invertExpected) {
        //Resource w = r.getRequiredProperty(IguanaVocab.workload).getObject().asResource();
        TestCase testCase = parseTestCase(w, invertExpected);

        TaskImpl result;
        if(o instanceof ContainmentSolver) {
            result = prepareTaskJena3(testCase, (ContainmentSolver)o);
        } else if(o instanceof LegacyContainmentSolver) {
            result = prepareTaskJena2(testCase, (LegacyContainmentSolver) o);
        } else {
            throw new RuntimeException("Unknown task type: " + o);
        }

        return result;
    }

    public static TaskImpl prepareTaskJena3(TestCase testCase, ContainmentSolver solver) {

//        Query _viewQuery = QueryTransformOps.transform(viewQuery, QueryUtils.createRandomVarMap(_viewQuery, "x"));
//        Query _userQuery = QueryTransformOps.transform(userQuery, QueryUtils.createRandomVarMap(_userQuery, "y"));

        return new TaskImpl(
            testCase,
	        () -> { // try {
	        	boolean hack = false;
	        	boolean actual;
	        	if(hack) {
		            System.out.println("HACK - Testing only our tool for debugging - results for other tools are garbage!!!");
		        	actual = SparqlQueryContainmentUtils.tryMatch(testCase.getSource(), testCase.getTarget());
	        	} else {
	        		actual = solver.entailed(testCase.getSource(), testCase.getTarget());
	        	}
        		
	        	//boolean actual = solver.entailed(testCase.getSource(), testCase.getTarget());

        		
	            //boolean actual = solver.entailed(testCase.getTarget(), testCase.getSource());
	            //String str = actual == testCase.getExpectedResult() ? "CORRECT" : "WRONG";
	            //System.out.println(str);
	            return actual;
	            //r.addLiteral(RDFS.label, str);
	            // } catch (ContainmentTestException e) {
	            // throw new RuntimeException(e);
	            // }
	        }, () -> {
	            try {
	                solver.cleanup();
	            } catch (ContainmentTestException e) {
	                throw new RuntimeException();
	            }
	        });
    }

}
