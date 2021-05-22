package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.sparql.ext.json.E_JsonPath;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.sparql.util.IterLib;

import com.google.gson.JsonObject;

/**
 * "SELECT ... { }" sys:benchmark(?time ?size)
 * 
 * @author raven
 *
 */
public class PropertyFunctionFactoryBenchmark
	implements PropertyFunctionFactory
{
//	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryBenchmark.class);

	protected Function<? super Path, ? extends Stream<? extends Path>> fn;

    @Override
    public PropertyFunction create(final String uri)
    {
    	return new PropertyFunctionBenchmark();
    }


    public static class PropertyFunctionBenchmark
    	extends PFuncSimpleAndList
    {

		@Override
		public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg object,
				ExecutionContext execCxt) {

			RDFConnection conn = E_Benchmark.getConnection(execCxt);
			boolean includeResultSet = object.getArgListSize() >= 3;
			JsonObject json = E_Benchmark.benchmark(conn, subject, includeResultSet);
			
			QueryIterator result;
			if(json == null) {
				//throw new ExprTypeException("no node value obtained");
				result = IterLib.noResults(execCxt);
			} else {
	
				Node timeNode = object.getArg(0);
				Node sizeNode = object.getArg(1);
				Node resultSetNode = includeResultSet ? object.getArg(2) : null;
	
	            BindingMap b = BindingFactory.create(binding) ;
	
// TODO Raise an exception if output arguments are non-variables
	            
//	            if(timeNode.isVariable()) {
		            b.add((Var)timeNode,
							NodeValue.makeDecimal(json.get("time").getAsBigDecimal()).asNode());
//	            }
	            
//	            if(sizeNode.isVariable()) {
		            b.add((Var)sizeNode,
							NodeValue.makeInteger(json.get("size").getAsBigInteger()).asNode());
//	            }
	            
//		            && resultSetNode.isVariable()
	            if(resultSetNode != null) {
		            b.add((Var)resultSetNode,
							E_JsonPath.jsonToNode(json.get("result")));
	            }
	            
	            result = QueryIterPlainWrapper.create(Iterators.singletonIterator(b), execCxt) ;
			}
			return result;
		}
    	
//		@Override
//        public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object,
//                org.apache.jena.sparql.engine.ExecutionContext execCtx) {
//            // Get the subject's value
//            Node node = subject.isVariable()
//                    ? binding.get((Var)subject)
//                    : subject;
//
//            if(!object.isVariable()) {
//                throw new RuntimeException("Object of json array splitting must be a variable");
//            }
//            Var outputVar = (Var)object;
//            
//            Iterator<Binding> bindings = Collections.emptyIterator();
//            try {
//            	if(node.isURI()) {
//            		String str = node.getURI();
//            		Path root = Paths.get(new URI(str));
//            		bindings = fn.apply(root)
//    					.map(path -> BindingFactory.binding(
//    							binding,
//    							outputVar,
//    							NodeFactory.createURI(path.toUri().toString())))
//    					.iterator();
//            		
////            		while(bindings.hasNext()) {
////            			System.out.println(bindings.next());
////            		}
//            	}
//            } catch(Exception e) {
//            	logger.warn("Error resolving node as URI: " + node, e);
//            }
//            
//            QueryIterator result = new QueryIterPlainWrapper(bindings);
//            return result;
//        }
    };
}