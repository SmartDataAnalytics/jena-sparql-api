package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.sparql.util.IterLib;

/**
 * "SELECT ... { }" sys:benchmark(?time ?size)
 * 
 * @author raven
 *
 */
public class PropertyFunctionFactoryExecSelect
	implements PropertyFunctionFactory
{
//	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryBenchmark.class);

	protected Function<? super Path, ? extends Stream<? extends Path>> fn;

    @Override
    public PropertyFunction create(final String uri)
    {
    	return new PropertyFunctionExecSelect();
    }


    public static class PropertyFunctionExecSelect
    	extends PFuncSimpleAndList
    {

		@Override
		public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg object,
				ExecutionContext execCxt) {
			QueryIterator result;
			
			RDFConnection conn = E_Benchmark.getConnection(execCxt);
			
			if(subject.isVariable()) {
				result = IterLib.noResults(execCxt);//.result(binding, execCxt);
			} else {
				NodeValue nv = NodeValue.makeNode(subject);
				if(nv.isString()) {
					String queryStr = nv.getString();
					QueryExecution qe = null;
					try {
						qe = conn.query(queryStr);
						ResultSet rs = qe.execSelect();
	//					rs = new ResultSetMem(tmp);
		
						List<String> resultVars = rs.getResultVars();
						int numRsVars = resultVars.size();
						List<Node> args = object.getArgList();
						
						Iterator<Binding> it = Streams.stream(rs).map(qs -> {
			            	BindingMap b = BindingFactory.create(binding);
		
			            	for(int i = 0; i < args.size(); ++i) {
			            		String rsVarName = i < numRsVars ? resultVars.get(i) : null;
			            		Node arg = args.get(i);
			            		Node val = Optional.ofNullable(qs.get(rsVarName)).map(RDFNode::asNode).orElse(null);
	//		            				arg.isVariable()
	//		            				? Optional.ofNullable(qs.get(rsVarName)).map(RDFNode::asNode).orElse(null)
	//		            				: arg;
		
			            		//b.add(Var.alloc(rsVarName), val);
			            		if(arg.isVariable() && val != null) {
			            			b.add((Var)arg, val);
			            		}
			            	}
			            	//System.out.println("Binding: " + b);
			            	return (Binding)b;
						}).iterator();
			            	
						
						result = QueryIterPlainWrapper.create(
		                		new IteratorClosable<Binding>(it, qe::close), execCxt);
					} catch(Exception e) {
						if(qe != null) {
							qe.close();
						}
				    	throw new ExprTypeException("Problem encountered", e);
					}
			    } else {
			    	throw new ExprTypeException("String argument expected");
			    }
			}
			
			return result;
		}
    };
}