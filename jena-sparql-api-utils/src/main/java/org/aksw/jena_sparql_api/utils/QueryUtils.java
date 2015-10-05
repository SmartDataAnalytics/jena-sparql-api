package org.aksw.jena_sparql_api.utils;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.Element;

import java.util.HashSet;
import java.util.Set;

public class QueryUtils {

    public static Query fixVarNames(Query query) {
        Query result = query.cloneQuery();

        Element element = query.getQueryPattern();
        Element repl = ElementUtils.fixVarNames(element);

        result.setQueryPattern(repl);
        return result;
    }


    /**
     *
     *
     * @param pattern    a pattern of a where-clause
     * @param resultVar  an optional result variable (used for describe queries)
     * @return
     */
    public static Query elementToQuery(Element pattern, String resultVar) {

        if ( pattern == null )
            return null ;
        Query query = new Query() ;
        query.setQueryPattern(pattern) ;
        query.setQuerySelectType() ;

        if(resultVar == null) {
            query.setQueryResultStar(true) ;
        }

        query.setResultVars() ;

        if(resultVar != null) {
            query.getResultVars().add(resultVar);
        }

        return query ;

    }

    public static Query elementToQuery(Element pattern)
    {
        return elementToQuery(pattern, null);
    }

    /**
     * This method does basically the same as
     *     com.hp.hpl.jena.sparql.engine.QueryExecutionBase.execConstruct
     *     and SparqlerBaseSelect
     * note sure if it is redundant
     *
     * @param quads
     * @param binding
     * @return
     */
    public static Set<Quad> instanciate(Iterable<Quad> quads, Binding binding)
    {
        Set<Quad> result = new HashSet<Quad>();
        Node nodes[] = new Node[4];
        for(Quad quad : quads) {
            for(int i = 0; i < 4; ++i) {
                Node node = QuadUtils.getNode(quad, i);

                // If the node is a variable, then substitute it's value
                if(node.isVariable()) {
                    node = binding.get((Var)node);
                }

                // If the node is null, or any non-object position
                // gets assigned a literal then we cannot instanciate
                if(node == null || (i < 3 && node.isLiteral())) {
                    result.clear();
                    return result;
                }

                nodes[i] = node;
            }

            Quad inst = QuadUtils.create(nodes);
            result.add(inst);
        }

        return result;
    }

}
