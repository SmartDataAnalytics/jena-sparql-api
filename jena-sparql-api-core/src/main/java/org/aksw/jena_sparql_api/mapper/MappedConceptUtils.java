package org.aksw.jena_sparql_api.mapper;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;

import com.hp.hpl.jena.sparql.expr.ExprVar;

public class MappedConceptUtils {
    
//    public static Iterator<NodeValue> execMapped(QueryExecutionFactory qef, MappedConcept mappedConcept) {
//        Concept concept = mappedConcept.getConcept();
//        Query query = concept.asQuery();
//        query.setQueryResultStar(true);

        
        
    //}
    /*
    public MappedConcept createListGraphs() {
        Concept concept = ConceptUtils.listGraphs();        
        AggBase aggregator = new AggLiteral(new ExprVar(concept.getVar()));
        
        MappedConcept result = new MappedConcept(concept, aggregator);
        return result;
    }
    */
}
