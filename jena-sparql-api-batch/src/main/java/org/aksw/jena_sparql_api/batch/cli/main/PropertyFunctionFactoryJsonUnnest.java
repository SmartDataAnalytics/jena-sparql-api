package org.aksw.jena_sparql_api.batch.cli.main;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.json.E_JsonPath;
import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory;

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
    @Override
    public PropertyFunction create(final String uri)
    {
        return new PFuncSimple()
        {
			@Override
			public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object,
					com.hp.hpl.jena.sparql.engine.ExecutionContext execCtx) {
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
							NodeValue nv = E_JsonPath.jsonToNodeValue(item);

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