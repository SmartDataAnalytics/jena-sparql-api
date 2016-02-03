package org.aksw.jena_sparql_api_sparql_path2;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropFuncArgType;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval;

/**
 * Select ?path {
 *   ?s path:find (?expr ?path ?k ?target)
 * }
 *
 * <ul>
 *   <li>?s: The start node for which to find paths</li>
 *   <li>?expr: A string representation of a property expression</li>
 *   <li>?path: The target variable, this variable will be bound to</li>
 *   <li>?target: A target node the property path must terminate it</li>
 * </ul>
 * ?path i
 *
 *
 * @author raven
 *
 */
public class PropertyFunctionEvalPaths
    extends PropertyFunctionEval
{

    protected PropertyFunctionEvalPaths(PropFuncArgType subjArgType,
            PropFuncArgType objFuncArgType) {
        super(subjArgType, objFuncArgType);
        // TODO Auto-generated constructor stub
    }

    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject,
            Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        //execCxt.getExecutor().
        OpExecutor x;
        //argObject.is
        // TODO Auto-generated method stub
        return null;
    }

}
