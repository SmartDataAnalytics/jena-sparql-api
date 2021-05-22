package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.sparql.ext.xml.PropertyFunctionFactoryXmlUnnest;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

/**
 * Recursively list URLs of directory content
 * 
 * @author Claus Stadler, Dec 5, 2018
 *
 */
public class PropertyFunctionFactoryFsFind
	implements PropertyFunctionFactory
{

	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryFsFind.class);

	protected Function<? super Path, ? extends Stream<? extends Path>> fn;

    public PropertyFunctionFactoryFsFind(Function<? super Path, ? extends Stream<? extends Path>> fn) {
        super();
    	this.fn = fn;
    }
    
    public static Stream<Path> parents(Path path) {
    	try {
	    	return Streams.stream(Traverser.<Path>forTree(p -> p == null || p.getParent() == null
	    				? Collections.emptySet()
	    				: Collections.singleton(p.getParent())
	    			).depthFirstPreOrder(path));
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }

    public static Stream<Path> find(Path path) {
    	try {
	    	return Files.list(path).flatMap(f -> {
	    		Stream<Path> r = Stream.of(f);
	    		
	    		Stream<Path> s = null;
	    		try {
		    		if(Files.isDirectory(f)) {
	    				s = find(f);
		    		}
	    		} catch (Exception e) {
	    			logger.warn("Could not access path", e);
	    		}

	    		r = s == null ? r : Stream.concat(r, s);

	    		return r;
	    	});
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }

    @Override
    public PropertyFunction create(final String uri)
    {
    	return new PathFunction(fn);
    }


    public static class PathFunction
    	extends PFuncSimple
    {
    	protected Function<? super Path, ? extends Stream<? extends Path>> fn;
    	
        public PathFunction(Function<? super Path, ? extends Stream<? extends Path>> fn) {
			super();
			this.fn = fn;
		}

		@Override
        public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object,
                org.apache.jena.sparql.engine.ExecutionContext execCtx) {
            // Get the subject's value
            Node node = subject.isVariable()
                    ? binding.get((Var)subject)
                    : subject;

            if(!object.isVariable()) {
                throw new RuntimeException("Object of json array splitting must be a variable");
            }
            Var outputVar = (Var)object;
            
            Iterator<Binding> bindings = Collections.emptyIterator();
            try {
            	if(node.isURI()) {
            		String str = node.getURI();
            		Path root = Paths.get(new URI(str));
            		bindings = fn.apply(root)
    					.map(path -> BindingFactory.binding(
    							binding,
    							outputVar,
    							NodeFactory.createURI(path.toUri().toString())))
    					.iterator();
            		
//            		while(bindings.hasNext()) {
//            			System.out.println(bindings.next());
//            		}
            	}
            } catch(Exception e) {
            	logger.warn("Error resolving node as URI: " + node, e);
            }
            
            QueryIterator result = QueryIterPlainWrapper.create(bindings);
            return result;
        }
    };
}
