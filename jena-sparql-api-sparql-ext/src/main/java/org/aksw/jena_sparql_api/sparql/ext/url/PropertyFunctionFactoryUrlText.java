package org.aksw.jena_sparql_api.sparql.ext.url;

import java.util.Collections;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * <uri> uri:resolve ?output
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryUrlText
    implements PropertyFunctionFactory {

	
	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryUrlText.class);

	
    protected Gson gson;

    public PropertyFunctionFactoryUrlText() {
        this(new Gson());
    }

    public PropertyFunctionFactoryUrlText(Gson gson) {
        super();
        this.gson = gson;
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
                
                List<Binding> bindings;
                try {
                	NodeValue nv = E_UrlText.resolve(NodeValue.makeNode(node));
                	
                    bindings = Collections.singletonList(BindingFactory.binding(binding, outputVar, nv.asNode()));
                    
                } catch(Exception e) {
                	logger.warn("Error resolving node as URI: " + node, e);

                	bindings = Collections.emptyList();
                }


//            	Map<String, String> overrides = execCtx.getContext().get("urlTextOverrides", Collections.emptyMap());
//            	if()


//                boolean isJson = node.isLiteral() && node.getLiteralDatatype() instanceof RDFDatatypeJson;
//                if(isJson) {
//                    JsonElement data = (JsonElement)node.getLiteralValue();
//                    if(data.isJsonArray()) {
//                        JsonArray arr = data.getAsJsonArray();
//                        List<Binding> bindings = new ArrayList<Binding>(arr.size());
//
//                        for(JsonElement item : arr) {
//                            NodeValue nv = E_JsonPath.jsonToNodeValue(item, gson);
//
//                            Binding b = BindingFactory.binding(binding, outputVar, nv.asNode());
//                            bindings.add(b);
//                        }
//
//                        result = new QueryIterPlainWrapper(bindings.iterator());
//                    }
//                }

                QueryIterator result = QueryIterPlainWrapper.create(bindings.iterator());
                return result;
            }
        };
    }
}