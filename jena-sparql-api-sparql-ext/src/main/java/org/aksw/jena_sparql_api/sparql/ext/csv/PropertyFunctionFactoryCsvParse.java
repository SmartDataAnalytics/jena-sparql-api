package org.aksw.jena_sparql_api.sparql.ext.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropFuncArgType;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * Function for parsing a given CSV resource as a stream of JSON objects
 * 
 * By default, the resource will be attempted to parse as EXCEL csv.
 * 
 * {
 *    <schema://url/to/data> csv:parse ?rowAsJson .
 *    <schema://url/to/data> csv:parse (?rowAsJson ["options"])
 * }
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryCsvParse
    implements PropertyFunctionFactory
{
	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryCsvParse.class);

	@Override
    public PropertyFunction create(final String uri)
    {
        return new PropertyFunctionEval(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_EITHER) {
        	
			@Override
		    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject,
		            Node predicate, PropFuncArg argObject, ExecutionContext execCtx) {
				
				Node subject = argSubject.getArg();
				
                Node node = subject.isVariable()
                        ? binding.get((Var)subject)
                        : subject;

                Node object;
                Node options = NodeFactory.createLiteral("");
                        
                if(argObject.isList()) {
                	List<Node> argList = argObject.getArgList();
                	int l = argList.size();
                	if(l == 0 || l > 2) {
                		throw new RuntimeException("One or two arguments expected");
                	}

                	object = argList.get(0);
                	options = l == 2 ? argList.get(1) : options;	                        
                } else {
	                object = argObject.getArg();
                }
                
                if(!object.isVariable()) {
                    throw new RuntimeException("Object of csv parsing must be a variable");
                }

                
                Var outputVar = (Var)object;

                Reader reader = null;
                //Runnable closeAction = null;
                InputStream in = null;
                if(subject.isLiteral()) {
                	// Create a reader for the string literal
                	boolean isString = subject.getLiteralDatatype().getURI().equals(XSD.xstring.getURI());
                	if(isString) {
                		String str = subject.getLiteralValue().toString();
                		reader = new StringReader(str);
                	}
                } else if(subject.isURI()) {
                	try {
	                	String str = subject.getURI();
	                	URI uri = new URI(str);
	                	URL url = uri.toURL();
	                	in = url.openStream();
	                	//closeAction = () -> { try { in.close(); } catch(Exception e) { throw new RuntimeException(e); } };
                	} catch(Exception e) {
                		throw new RuntimeException(e);
                	}
                	// TODO Maybe we need to add encoding support
                	reader = new InputStreamReader(in);
                }
                
                String optionStr = options.getLiteralValue().toString();

                Stream<JsonElement> jsonObjStream;
				try {
					jsonObjStream = E_CsvParse.parseCsv(reader, optionStr);
				} catch (IOException e) {
					logger.warn("Failed to process csv input", e);
					jsonObjStream = Collections.<JsonElement>emptySet().stream();
				}
                
                QueryIterator result = QueryIterPlainWrapper.create(
            		new IteratorClosable<>(
            				jsonObjStream
		            		.map(RDFDatatypeJson::jsonToNode)
		            		.map(n -> BindingFactory.binding(outputVar, n))
		            		.iterator(),
		            in));
                        		                

//                if(result == null) {
//                    result = QueryIterNullIterator.create(execCtx);
//                }

                return result;
            }
        };
    }
}