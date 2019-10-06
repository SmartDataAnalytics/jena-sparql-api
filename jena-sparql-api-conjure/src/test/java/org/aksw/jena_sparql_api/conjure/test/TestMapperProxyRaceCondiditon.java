package org.aksw.jena_sparql_api.conjure.test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Test;

public class TestMapperProxyRaceCondiditon {
	
	/**
	 * Test for ISSUE #30 where in a concurrent setting, Resource.getModel()
	 * on a proxied resource would
	 * occasionally return another resource's model. It is likely that any method call
	 * via the mapper-proxy system is susceptible to getting wrongly forward. 
	 * 
	 * At present, a fix using synchronization is in place.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void Test() throws InterruptedException, ExecutionException {
		
	    // System.identityHashCode may give different results when the JVM moved objects in memory
	    // In that case, we recheck whether after a detected change
		// we can reproduce the changed value with another call to System.identityHashCode
		// If so, we consider that a recovery, otherwise, something is very likely to have gone wrong

		
		Runnable runnable = () -> {
			long id = Thread.currentThread().getId();
			for(int i = 0; i < 100; ++i) {
			    Model m = ModelFactory.createDefaultModel();	    
			    int a = System.identityHashCode(m);
			    //System.out.println(id + " a: " + m.size() + " " + a);
			    
			    Resource x = m.createResource();
			    int b = System.identityHashCode(x.getModel());
			    //System.out.println(id + " b: " + x.getModel().size() + " " + b + " <- " + a);
		        
			    // Problematic line: Sometimes y's model becomes that of another thread
			    Resource y = x.as(DataRefUrl.class);
			    int c = System.identityHashCode(y.getModel());
			 
		        if(c == b) {
		        	// Ok
			        //System.out.println(id + " c: " + y.getModel().size() + " " + c + " <- " + b + " ");
		        } else {
				    // Recheck
			    	int b2 = System.identityHashCode(x.getModel());
			    	if(c != b2) {
			    		// Error
			    		String msg = id + " c: " + y.getModel().size() + " " + c + " <- " + b + " rechecked: " + b2 + " ERROR ";
			    		//System.err.println(msg);
				        throw new RuntimeException("Race condition detected: " + msg);
			    	} 
			    	else {
				    	//System.out.println(id + " c: " + y.getModel().size() + " " + c + " <- " + b2 + " RECOVERED");			    		
			    	}
			    }
			}
		};
		
		int numThreads = 4;
		ExecutorService es = Executors.newFixedThreadPool(numThreads);
		List<Future<?>> futures = IntStream.range(0, numThreads)
			.mapToObj(i -> es.submit(runnable))
			.collect(Collectors.toList());
		
		es.shutdown();
		es.awaitTermination(3, TimeUnit.SECONDS);
//		es.awaitTermination(10, TimeUnit.MINUTES);
		
		for(Future<?> f : futures) {
			try {
				f.get();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
