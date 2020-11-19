package org.aksw.jena_sparql_api.algebra.transform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * This transform is NOT yet implemented; in general the extraction of constants must ensure
 * that the cardinality is preserved.
 *
 *
 * Given a table extract all variables that map to the same constant to a bind block:
 *
 * Given:
 *
 * VALUES (?x ?y) {
 *   (a b1)
 *   (a b2)
 * }
 *
 * it becomes:
 *
 * VALUES (?y) { b1 b2 )
 * BIND(a AS ?x)
 *
 */
public class TransformTableToExtend
    extends TransformCopy
{
//    @Override
//    public Op transform(OpTable opTable) {
//        Table table = opTable.getTable();
//        Map<Var, Node> constants = extractConstants(table.toResultSet());
//
//        Op result;
//        if (constants.isEmpty()) {
//            result = opTable;
//        } else {
//        	Set<>table.getVars()
//        }
//
//        return result;
//    }


    /**
     * Yield all variable-value pairs where the variable is mapped to the same
     * value across all given bindings. The value may be null.
     *
     * @param rs
     * @return
     */
    public static Map<Var, Node> extractConstants(ResultSet rs) {
        Set<Var> candVars = rs.getResultVars().stream()
                .map(Var::alloc).collect(Collectors.toSet());

        Map<Var, Node> result = new HashMap<>();
        while (rs.hasNext()) {
            Binding b = rs.nextBinding();

            Iterator<Var> itVar = candVars.iterator();
            while (itVar.hasNext()) {
                Var v = itVar.next();
                Node n = b.get(v);

                // We need to use containsKey because null values are allowed
                if (result.containsKey(v)) {
                    Node prev = result.get(v);
                    if (!Objects.equals(prev, n)) {
                        itVar.remove();
                        result.remove(v);

                        if (candVars.isEmpty()) {
                            break;
                        }
                    }
                } else {
                    result.put(v, n);
                }
            }
        }

        return result;
    }
}
