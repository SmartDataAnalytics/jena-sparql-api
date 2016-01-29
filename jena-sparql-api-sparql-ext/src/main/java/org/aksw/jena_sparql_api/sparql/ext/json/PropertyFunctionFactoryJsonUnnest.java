package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

/**
 * {
 *    Bind("['foo', 'bar']"^^xsd:json As ?json)
 *    ?json json:array ?items.
 * }
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryJsonUnnest implements PropertyFunctionFactory {

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

                QueryIterator result = null;

                boolean isJson = node.isLiteral() && node.getLiteralDatatype() instanceof RDFDatatypeJson;
                if(isJson) {
                    JsonElement data = (JsonElement)node.getLiteralValue();
                    if(data.isJsonArray()) {
                        JsonArray arr = data.getAsJsonArray();
                        List<Binding> bindings = new ArrayList<Binding>(arr.size());

                        for(JsonElement item : arr) {
                            NodeValue nv = E_JsonPath.jsonToNodeValue(item, gson);

                            Binding b = BindingFactory.binding(binding, outputVar, nv.asNode());
                            bindings.add(b);
                        }

                        result = new QueryIterPlainWrapper(bindings.iterator());
                    }
                }

                if(result == null) {
                    result = QueryIterNullIterator.create(execCtx);
                }

                return result;
            }
        };
    }
}