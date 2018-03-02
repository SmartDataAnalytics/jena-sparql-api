package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * {
 *    Bind("['foo', 'bar']"^^xsd:json As ?json)
 *    ?json json:array ?items.
 * }
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryJsonUnnest
    implements PropertyFunctionFactory {

    protected Gson gson;

    public PropertyFunctionFactoryJsonUnnest() {
        this(new Gson());
    }

    public PropertyFunctionFactoryJsonUnnest(Gson gson) {
        super();
        this.gson = gson;
    }

    @Override
    public PropertyFunction create(final String uri)
    {
        return new PFuncSimpleAndList()
        {
            @Override
			public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg objects,
					ExecutionContext execCxt) {

            	// Get the subject's value
                Node node = subject.isVariable()
                        ? binding.get((Var)subject)
                        : subject;

                Node object = objects.getArg(0);
                        
                if(!object.isVariable()) {
                    throw new RuntimeException("Object of json array unnesting must be a variable");
                }
                Var outputVar = (Var)object;

                Node index = objects.getArgListSize() > 1 ? objects.getArg(1) : null;
                if(index != null && !index.isVariable()) {
                    throw new RuntimeException("Index of json array unnesting must be a variable");
                }
                Var indexVar = (Var)index;

                
                QueryIterator result = null;

                boolean isJson = node.isLiteral() && node.getLiteralDatatype() instanceof RDFDatatypeJson;
                if(isJson) {
                    JsonElement data = (JsonElement)node.getLiteralValue();
                    if(data.isJsonArray()) {
                        JsonArray arr = data.getAsJsonArray();
                        List<Binding> bindings = new ArrayList<Binding>(arr.size());

                        //for(JsonElement item : arr) {
                        for(int i = 0; i < arr.size(); ++i) {
                        	JsonElement item = arr.get(i);
                        	RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

                            Node n = E_JsonPath.jsonToNode(item, gson, jsonDatatype);
                            NodeValue nv = NodeValue.makeNode(n);

                            Binding b = BindingFactory.binding(binding, outputVar, nv.asNode());
                            
                            if(index != null) {
                            	b = BindingFactory.binding(b, indexVar, NodeValue.makeInteger(i).asNode());
                            }

                            bindings.add(b);
                        }

                        result = new QueryIterPlainWrapper(bindings.iterator());
                    }
                }

                if(result == null) {
                    result = QueryIterNullIterator.create(execCxt);
                }

                return result;
            }
        };
    }
}