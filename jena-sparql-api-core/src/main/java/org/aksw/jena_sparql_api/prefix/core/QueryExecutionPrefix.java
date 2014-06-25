package org.aksw.jena_sparql_api.prefix.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/**
 */
// https://www.mail-archive.com/virtuoso-users@lists.sourceforge.net/msg01990.html
//public class QueryExecutionPrefix
//    extends QueryExecutionFactoryDecorator {
//
//    // The mappings are applied in this order (first defaults, the override, then removals)
//    private PrefixMapping defaults;
//    private PrefixMapping overrides;
//    //private PrefixMapping removals;
//    //Set<String> removals;
//    
//    public QueryExecutionPrefix(QueryExecutionFactory queryExecutionFactory) {
//        super(queryExecutionFactory);
//    }
//    
//    @Override
//    public QueryExecution createQueryExecution(Query query) {
//        return result;
//
//        return decoratee.createQueryExecution(query);
//    }
//
//    @Override
//    public QueryExecution createQueryExecution(String queryString) {
//        PrefixMapping pm = new PrefixMappingImpl();
//        pm.setNsPrefixes(defaults);
//        
//        pm.setNsPrefixes(those of the query);
//        
//        
//        Query query = QueryFactory.create(queryString);
//        
//        QueryExecution result = createQueryExecution(query);
//        
//        //return decoratee.createQueryExecution(queryString);
//        
//    }
//}
