package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;

/**
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
public class TransformFactorizeTableColumnsToExtend
    extends TransformCopy
{
    /**
     * Virtuoso does not support an empty table - whereas jena does.
     * I.e. the following works with jena but not with virtuoso:
     *
     * SELECT * { VALUES () { () () () } BIND (<urn:x> AS ?x) }
     *
     */
    protected boolean preventEmptyTable = true;

    @Override
    public Op transform(OpTable opTable) {
        Table table = opTable.getTable();
        Map<Var, Node> constants = extractConstants(table.toResultSet());

        Set<Var> tableVars = new LinkedHashSet<>(table.getVars());
        Set<Var> constantVars = constants.keySet();

        if (constantVars.containsAll(tableVars) && preventEmptyTable && !tableVars.isEmpty()) {
            // The following statement implicitly removes the entry from 'constants'
            constantVars.remove(tableVars.iterator().next());
        }

        Op result;
        if (constants.isEmpty()) {
            result = opTable;
        } else {
            VarExprList vel = new VarExprList();

//            VarExprListUtils.createFromMap(map)
            for (Entry<Var, Node> e : constants.entrySet()) {
                vel.add(e.getKey(), NodeValue.makeNode(e.getValue()));
            }

            List<Var> remainingTableVars = new ArrayList<>(Sets.difference(tableVars, constantVars));
            OpTable newTable = TransformEvalTable.create().exec(new OpProject(opTable, remainingTableVars));

            result = OpExtend.extend(newTable, vel);
        }

        return result;
    }


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
