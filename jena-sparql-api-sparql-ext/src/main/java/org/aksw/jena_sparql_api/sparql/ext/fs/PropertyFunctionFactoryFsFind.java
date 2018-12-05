package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
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

/**
 * Recursively list URLs of directory content
 * 
 * @author Claus Stadler, Dec 5, 2018
 *
 */
public class PropertyFunctionFactoryFsFind
	implements PropertyFunctionFactory
{

	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryXmlUnnest.class);
        
    public PropertyFunctionFactoryFsFind() {
        super();
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
        return new PFuncSimple()
        {
        	
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
                		bindings = find(root)
        					.map(path -> BindingFactory.binding(
        							binding,
        							outputVar,
        							NodeFactory.createURI(path.toUri().toString())))
        					.iterator();
                		
//                		while(bindings.hasNext()) {
//                			System.out.println(bindings.next());
//                		}
                	}
                } catch(Exception e) {
                	logger.warn("Error resolving node as URI: " + node, e);
                }
                
                QueryIterator result = new QueryIterPlainWrapper(bindings);
                return result;
            }
        };
    }
}
