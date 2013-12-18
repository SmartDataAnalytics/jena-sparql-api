package org.aksw.jena_sparql_api.cache.staging;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.h2.jdbcx.JdbcDataSource;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;





public class TestCachePostgres {
	public static void main(String[] args) {
		 JdbcDataSource dataSource = new JdbcDataSource();
		 dataSource.setURL("jdbc:h2:/test");
		 dataSource.setUser("sa");
		 dataSource.setPassword("sa");


		 CacheBackendDao cacheBackendDao = new CacheBackendDaoPostgres();
		 CacheBackend cacheBackend = new CacheBackendDataSource(dataSource, cacheBackendDao);
		 CacheFrontend cacheFrontend = new CacheFrontendImpl(cacheBackend);
		 
		 QueryExecutionFactory sparqlService = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql");
		 sparqlService = new QueryExecutionFactoryCacheEx(sparqlService, cacheFrontend);

		 QueryExecution qe = sparqlService.createQueryExecution("Select ?s { ?s a <http://dbpedia.org/ontology/Castle> }");
		 ResultSet rs = qe.execSelect();
		 while(rs.hasNext()) {
			 Binding binding = rs.nextBinding();
			 Node node = binding.get(Var.alloc("s"));
			 System.out.println(node);
		 }
		 
	}
}
