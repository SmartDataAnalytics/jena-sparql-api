package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.springframework.util.Assert;




interface SparqlCache {
    Map<Op, Table> get(Op op);
}

class SparqlCacheImpl
    implements SparqlCache
{
    Map<OpSummary, Map<Op, Table>> summaryToOpToTable;

    @Override
    public Map<Op, Table> get(Op op) {
        OpSummary summary = OpUtils.createSummary(op);

        Map<Op, Table> opToTable = summaryToOpToTable.get(summary);

        Map<Op, Table> result = new HashMap<Op, Table>();

        for(Entry<Op, Table> entry : opToTable.entrySet()) {
            Op cacheOp = entry.getKey();
            Table table = entry.getValue();

            boolean isEquivalent = OpUtils.isEquivalent(op, cacheOp);
            if(isEquivalent) {
                result.put(op, table);
            }
        }

        return result;
    }
}

// Tag interface for OpSummaries
interface OpSummary
{
}

class OpSummaryImpl
    implements OpSummary
{
    private Map<String, Object> data;

    public OpSummaryImpl(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpSummaryImpl other = (OpSummaryImpl) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }
}

public class OpUtils {



    public static OpSummary createSummary(Op op) {
        Map<String, Object> summary = summarize(op);
        OpSummary result = new OpSummaryImpl(summary);
        return result;
    }

    public static Map<String, Object> summarize(Op op) {
        Map<String, Object> result = new HashMap<String, Object>();

        summarize(op, result);

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void increment(Map<K, V> map, K key) {
        V val = map.get(key);
        Integer v;
        if(val == null) {
            v = 1;
        } else {
            v = ((Number)val).intValue() + 1;
        }
        map.put(key, (V)v);
    }

    public static void summarize(Op op, Map<String, Object> result) {
        String label = FunctionUtils.getClassLabel(op);
        //result.put(label, true);
        increment(result, label);

        List<Op> subOps = OpUtils.getSubOps(op);
        for(Op subOp : subOps) {
            summarize(subOp, result);
        }
    }


    public static boolean isEquivalent(Op a, Op b) {

        boolean result = true;

        String cla = FunctionUtils.getClassLabel(a);
        String clb = FunctionUtils.getClassLabel(b);

        if(cla.equals(clb)) {
            List<Op> sas = getSubOps(a);
            List<Op> sbs = getSubOps(b);

            int n = sas.size();
            if(n == sbs.size()) {
                for(int i = 0; i < n; ++i) {
                    Op sa = sas.get(i);
                    Op sb = sbs.get(i);

                    boolean subResult = isEquivalent(sa, sb);
                    if(!subResult) {
                        result = false;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Traverse an op structure and create a map from each subOp to its immediate parent
     *
     * NOTE It must be ensured that common sub expressions are different objects,
     * since we are using an identity hash map for mapping children to parents
     *
     *
     * @param op
     * @return
     */
    public static Map<Op, Op> parentMap(Op rootOp) {
        Map<Op, Op> result = new IdentityHashMap<Op, Op>();

        result.put(rootOp, null);

        parentMap(rootOp, result);
        return result;
    }

    public static void parentMap(Op op, Map<Op, Op> result) {
        List<Op> subOps = getSubOps(op);

        for(Op subOp : subOps) {
            result.put(subOp, op);

            parentMap(subOp, result);
        }
    }


    public static Op copy(Op op, List<Op> subOps) {
        Op result;
        int l = subOps.size();
        if(op instanceof Op0) {
            Assert.state(l == 0);
            Op0 o = (Op0)op;
            result = o.copy();
        } else if (op instanceof Op1) {
            Assert.state(l == 1);
            Op1 o = (Op1)op;
            result = o.copy(subOps.get(0));
        } else if (op instanceof Op2) {
            Assert.state(l == 2);
            Op2 o = (Op2)op;
            result = o.copy(subOps.get(0), subOps.get(1));
        } else if (op instanceof OpN) {
            //Assert.state(subOps.size() == 0);
            OpN o = (OpN)op;
            result = o.copy(subOps);
        } else {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    public static List<Op> getSubOps(Op op) {
        List<Op> result;

        if(op instanceof Op0) {
            result = Collections.emptyList();
        } else if (op instanceof Op1) {
            result = Collections.singletonList(((Op1)op).getSubOp());
        } else if (op instanceof Op2) {
            Op2 tmp = (Op2)op;
            result = Arrays.asList(tmp.getLeft(), tmp.getRight());
        } else if (op instanceof OpN) {
            result = ((OpN)op).getElements();
        } else {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    /**
     * Traverses an op structure in order to determine whether it contains
     * any concrete triple or quad patterns.
     * If no such pattern is found, the query is independent of a dataset
     * and hence it can be evaluated e.g. against an empty in-memory model.
     *
     *
     * @param op
     * @return
     */
    public static boolean isPatternFree(Op op) {
        boolean isPattern =
                op instanceof OpQuadPattern ||
                op instanceof OpQuadBlock ||
                op instanceof OpTriple ||
                op instanceof OpBGP;

        boolean result;

        if(isPattern) {
            result = false;
        } else {
            List<Op> subOps = getSubOps(op);

            result = true;
            for(Op subOp : subOps) {
                boolean tmp = isPatternFree(subOp);
                if(tmp == false) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

}


