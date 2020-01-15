package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.aksw.jena_sparql_api.utils.Symbols;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class E_Benchmark
	extends FunctionBase1
{
	private static final Logger logger = LoggerFactory.getLogger(E_Benchmark.class);
	
	
	@Override
	protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
		Node node = args.get(0).asNode();
		RDFConnection conn = getConnection(env);
		JsonObject json = benchmark(conn, node, false);
		if(json == null) {
			throw new ExprTypeException("no node value obtained");
		}
		NodeValue result = RDFDatatypeJson.jsonToNodeValue(json);

	    return result;
	}

	public static JsonObject benchmark(RDFConnection conn, Node node, boolean includeResultSet) {
		JsonObject result;
		if(node.isVariable()) {
			result = null;
		} else {
			NodeValue nv = NodeValue.makeNode(node);
	
			if(nv.isString()) {
	    		String queryStr = nv.getString();
	    		result = benchmark(conn, queryStr, includeResultSet);
	    	} else { 	
		    	throw new ExprTypeException("Incorrect node value type " + nv);//": " + node)) ;
		    }
		}		
	    return result;
	}
	
	public static RDFConnection getConnection(ExecutionContext env) {
		RDFConnection conn = null;
		Context cxt = env.getContext();
		if(cxt != null) {
			conn = cxt.get(Symbols.symConnection);
		}
		
		if(conn == null) {
			logger.info("No connection in context, falling back to dataset");
			DatasetGraph dsg = env.getDataset();
			if(dsg != null) {
				Dataset ds = DatasetFactory.wrap(dsg);
				conn = RDFConnectionFactory.connect(ds);
			}
		}
		
		return conn;
	}


	public static RDFConnection getConnection(FunctionEnv env) {
		RDFConnection conn = null;
		Context cxt = env.getContext();
		if(cxt != null) {
			conn = cxt.get(Symbols.symConnection);
		}
		
		if(conn == null) {
			logger.info("No connection in context, falling back to dataset");
			DatasetGraph dsg = env.getDataset();
			if(dsg != null) {
				Dataset ds = DatasetFactory.wrap(dsg);
				conn = RDFConnectionFactory.connect(ds);
			}
		}
		
		return conn;
	}

	public static JsonObject benchmark(RDFConnection conn, String queryStr, boolean includeResultSet) {
		
		if(conn == null) {
			throw new RuntimeException("No connection or dataset specified in context");
		}

		logger.info("Benchmarking query: " + queryStr);
		
		Stopwatch sw = Stopwatch.createStarted();
		Long resultSetSize = null;
		ResultSetRewindable rsw = null;
		try(QueryExecution qe = conn.query(queryStr)) {
			ResultSet rs = qe.execSelect();
			
			if(includeResultSet) {
				rsw = ResultSetFactory.copyResults(rs);
			} else {				
				resultSetSize = (long)ResultSetFormatter.consume(rs);
			}
			
		} catch(Exception e) {
    		logger.warn("Failure executing benchmark request", e);
			throw new ExprTypeException("Failure executing benchmark request", e);
		}

		
		long ms = sw.stop().elapsed(TimeUnit.NANOSECONDS);
		BigDecimal s = new BigDecimal(ms).divide(new BigDecimal(1000000000l));

		JsonObject json = new JsonObject();
		json.addProperty("time", s);

		if(rsw != null) {
			rsw.reset();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsJSON(baos, rsw);

			String resultSetStr = baos.toString();

			resultSetSize = (long)rsw.size();
			Gson gson = new Gson();
			JsonElement el = gson.fromJson(resultSetStr, JsonElement.class);
			json.add("result", el);
		}

		json.addProperty("size", resultSetSize);

		return json;
	}

	@Override
	public NodeValue exec(NodeValue v) {
		throw new RuntimeException("Should not be invoked directly");
	}
}

//public class E_Benchmark
//	extends ExprFunctionOp
//{	
//    private static final String symbol = "benchmark";
//
//    public E_Benchmark(Op op) {
//        this(null, op) ;
//    }
//
//    public E_Benchmark(Element elt) {
//        this(elt, Algebra.compile(elt)) ;
//    }
//
//    public E_Benchmark(Element el, Op op) {
//        super(symbol, el, op) ;
//    }
//
//	@Override
//	protected NodeValue eval(Binding binding, QueryIterator iter, FunctionEnv env) {
//		return NodeValue.makeInteger(666);
//	}
//
//	
//
//    @Override
//    public Expr copySubstitute(Binding binding) {
//        Op op2 = Substitute.substitute(getGraphPattern(), binding) ;
//        return new E_Exists(getElement(), op2) ;
//    }
//
//    @Override
//    public Expr applyNodeTransform(NodeTransform nodeTransform) {
//        Op op2 = NodeTransformLib.transform(nodeTransform, getGraphPattern()) ;
//        return new E_Exists(getElement(), op2) ;
//    }
//
////    @Override
////    protected NodeValue eval(Binding binding, QueryIterator qIter, FunctionEnv env) {
////        boolean b = qIter.hasNext() ;
////        return NodeValue.booleanReturn(b) ;
////    }
//
//    @Override
//    public int hashCode() {
//        return symbol.hashCode() ^ getGraphPattern().hashCode() ;
//    }
//
//    @Override
//    public boolean equals(Expr other, boolean bySyntax) {
//        if ( other == null ) return false ;
//        if ( this == other ) return true ;
//        if ( ! ( other instanceof E_Benchmark ) )
//            return false ;
//        
//        E_Exists ex = (E_Exists)other ;
//        if ( bySyntax )
//            return this.getElement().equals(ex.getElement()) ;
//        else
//            return this.getGraphPattern().equals(ex.getGraphPattern()) ;
//    }
//    
//    @Override
//    public ExprFunctionOp copy(ExprList args, Op x) { return new E_Benchmark(x) ; }
//    
//    @Override
//    public ExprFunctionOp copy(ExprList args, Element elPattern) { return new E_Benchmark(elPattern) ; }
//}
