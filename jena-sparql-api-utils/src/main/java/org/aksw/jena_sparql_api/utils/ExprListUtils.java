package org.aksw.jena_sparql_api.utils;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.ExprUtils;

public class ExprListUtils {
	public static boolean contains(ExprList exprList, Expr expr) {
		boolean result = false;

		for(Expr item : exprList) {
			result = item.equals(expr);
			if(result) {
				break;
			}
		}

		return result;
	}

	public static ExprList fromUris(Iterable<String> uris) {
		List<Node> nodes = NodeUtils.fromUris(uris);
		ExprList result = nodesToExprs(nodes);
		return result;
	}

    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr e = ExprUtils.nodeToExpr(node);
            result.add(e);
        }

        return result;
    }
}
