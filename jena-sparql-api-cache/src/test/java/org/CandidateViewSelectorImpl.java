package org;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.util.Pair;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.sparqlify.database.Constraint;
import org.aksw.sparqlify.database.IndexMetaNode;
import org.aksw.sparqlify.database.IsPrefixOfConstraint;
import org.aksw.sparqlify.database.MetaIndexFactory;
import org.aksw.sparqlify.database.PrefixIndexMetaFactory;
import org.aksw.sparqlify.database.Table;
import org.aksw.sparqlify.database.TableBuilder;
import org.aksw.sparqlify.database.TreeIndex;
import org.apache.commons.collections15.Transformer;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class CandidateViewSelectorImpl<T>
    implements CandidateViewSelector<T>
{
    public static final String[] COLUMN_NAMES = new String[]{"g_prefix", "s_prefix", "p_prefix", "o_prefix"};

    protected Table<Object> table;

    protected boolean validateConstraintExpr = true;


    public static Table<Object> createDefaultTable() {
        TableBuilder<Object> builder = new TableBuilder<Object>();
        builder.addColumn("g_prefix", String.class);
        builder.addColumn("s_prefix", String.class);
        builder.addColumn("p_prefix", String.class);
        builder.addColumn("o_prefix", String.class);
        builder.addColumn("o_type", Integer.class);
        builder.addColumn("value", Object.class);

        Table<Object> result = builder.create();


        Transformer<Object, Set<String>> prefixExtractor = new Transformer<Object, Set<String>>() {
            @Override
            public Set<String> transform(Object input) {
                return Collections.singleton(input.toString());
            }

        };


        MetaIndexFactory factory = new PrefixIndexMetaFactory(prefixExtractor);
        //MetaIndexFactory factory = new PatriciaAccessorFactory(prefixExtractor);

        IndexMetaNode root = IndexMetaNode.create(result, factory, "s_prefix");
        IndexMetaNode s = IndexMetaNode.create(root, factory, "p_prefix");
        TreeIndex.attach(result, root);

        //IndexMetaNode o = IndexMetaNode.create(s, factory, "o");

        IndexMetaNode root2 = IndexMetaNode.create(result, factory, "p_prefix");
        IndexMetaNode s2 = IndexMetaNode.create(root2, factory, "s_prefix");
        TreeIndex.attach(result, root2);
        //IndexMetaNode o = IndexMetaNode.create(s, factory, "o");

        /*
        idxS = PrefixIndex.attach(prefixExtractor, table, "s_prefix");
        PrefixIndex.attach(, "s_prefix");

        idxTest = PrefixIndex.attach(prefixExtractor, table, "p_prefix");
        idxTest = PrefixIndex.attach(prefixExtractor, table, "o_prefix");
        */

        return result;
    }

    public static String mostSpecificSubstring(String a, String b) {
        int m = a.length();
        int n = b.length();

        boolean isALonger = m > n;
        String result = isALonger
                ? mostSpecificSubstring2(a, b)
                : mostSpecificSubstring2(b, a)
                ;
        return result;
    }

    public static String mostSpecificSubstring2(String a, String b) {
        String result = a.startsWith(b) ? a : null;
        return result;
    }

    public static String lessSpecificSubstring(String a, String b) {
        int m = a.length();
        int n = b.length();

        boolean isAShorter = m < n;
        String result = isAShorter
                ? lessSpecificSubstring2(a, b)
                : lessSpecificSubstring2(b, a)
                ;
        return result;
    }

    public static String lessSpecificSubstring2(String a, String b) {
        String result = b.startsWith(a) ? a : null;
        return result;
    }

    public static NavigableSet<String> intersectPrefixes(NavigableSet<String> as, NavigableSet<String> bs) {
        NavigableSet<String> result = as.stream()
            .flatMap(a ->
                bs.stream().map(b -> mostSpecificSubstring(a, b)))
        .filter(x -> x != null)
        .collect(Collectors.toCollection(TreeSet::new));

         return result;
    }

    public static NavigableSet<String> unionPrefixes(NavigableSet<String> as, NavigableSet<String> bs) {
        NavigableSet<String> result = as.stream()
            .flatMap(a ->
                bs.stream().map(b -> lessSpecificSubstring(a, b)))
        .filter(x -> x != null)
        .collect(Collectors.toCollection(TreeSet::new));

         return result;
    }



    public CandidateViewSelectorImpl() {
        super();
        this.table = createDefaultTable();
    }

//
//    public static Constraint deriveConstraint(Expr expr) {
//        if(expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive) {
//            return deriveConstraint(expr);
//        }
//
//        return null;
//    }
//
//    public static StartsWithConstraint deriveConstraint(E_StrConcat expr) {
//        return deriveConstraint(expr);
//    }
//
//    public static StartsWithConstraint deriveConstraint(E_StrConcatPermissive expr) {
//        return deriveConstraint(expr);
//    }
//
//
//    /**
//     * If a variable equals a (uri or string) constant, it means that the view must provide
//     * a prefix for that value.
//     *
//     * @param a
//     * @param b
//     * @return
//     */
//    public static VariableConstraint deriveIsPrefixOfConstraint(Expr a, Expr b)
//    {
//        if(!(a.isVariable() && b.isConstant())) {
//            return null;
//        }
//
//        Object value = NodeValueUtils.getValue(b.getConstant());
//
//
//        return new VariableConstraint(a.getVarName(), new IsPrefixOfConstraint(value.toString()));
//    }
//
//
//    /**
//     * Returns IsPrefixOf Constraints for equality expressions between variables and constants.
//     *
//     * Used for looking up view candidates.
//     * Not used for satisfiability checks.
//     *
//     *
//     * @param expr
//     * @return
//     */
//    public static VariableConstraint deriveViewLookupConstraint(Expr expr) {
//        if(expr instanceof E_Equals) {
//            E_Equals e = (E_Equals)expr;
//
//            VariableConstraint c = deriveIsPrefixOfConstraint(e.getArg1(), e.getArg2());
//            if(c == null) {
//                c = deriveIsPrefixOfConstraint(e.getArg2(), e.getArg1());
//            }
//
//            return c;
//        }
//        else {
//            return null;
//        }
//    }
//

    public static void validateConstraintExpr(Expr expr) {
        Set<Var> exprVars = ExprVars.getVarsMentioned(expr);
        boolean isValid = Vars.gspo.containsAll(exprVars);
        if(!isValid) {
            throw new RuntimeException("Constraint expressions may only use the variables g, s, p, and o");
        }
    }


    /**
     * Views are assumed to be indexed by each individual quad pattern for which they provide solutions.
     *
     * Use {@link QuadPrefixes.ALWAYS_MATCHING} to add a view that will match any lookup request
     *
     * @param decl
     * @param view
     */
    public void put(QuadPrefixes decl, T value) {
        List<Collection<?>> columnValues = new ArrayList<Collection<?>>(6);

        for(int i = 0; i < 4; ++i) {
            Collection<String> prefixes = decl.getPrefixes().get(i);
            if(prefixes == null) {
                prefixes = Collections.singleton("");
            }

            columnValues.add(prefixes);
        }

        Collection<Integer> termTypes =
                decl.isMayBeObjectResource()
                ? (decl.isMayBeObjectLiteral() ? Arrays.asList(1, 2) : Collections.singleton(1))
                : (decl.isMayBeObjectLiteral() ? Collections.singleton(2) : null)
                ;

        columnValues.add(termTypes);

        CartesianProduct<Object> cartesian = new CartesianProduct<Object>(columnValues);
        for(List<Object> item : cartesian) {
            List<Object> row = new ArrayList<Object>(item);
            row.add(value);
            table.add(row);
        }
    }

//    @Override
//    public Collection<T> apply(QuadPrefixes decl) {
//
//    }

    public Multimap<Var, Expr> indexExprsByVar(Set<Expr> exprs) {
        Multimap<Var, Expr> result = HashMultimap.create();
        for(Expr expr : exprs) {
            Set<Var> exprVars = ExprVars.getVarsMentioned(expr);
            if(exprVars.size() == 1) {
                Var var = Iterables.getFirst(exprVars, null);
                result.put(var, expr);
            }
        }
        return result;
    }

    public Map<String, Constraint> inferColumnConstraints(Set<Expr> dnfClause) {
        Map<String, Constraint> result = new HashMap<String, Constraint>();

        Multimap<Var, Expr> varToExprs = indexExprsByVar(dnfClause);

        Map<Var, Set<String>> varToPrefixes = new HashMap<>();
        for(int i = 0; i < 4; ++i) {

            Var var = Vars.gspo.get(i);
            String columnName = COLUMN_NAMES[i];
            Collection<Expr> exprs = varToExprs.get(var);

            for(Expr expr : exprs) {

                Pair<Var, NodeValue> e = ExprUtils.extractConstantConstraint(expr);
                if(e != null) {
                    Node node = e.getValue().asNode();
                    if(node.isURI()) {
                        result.put(columnName, new IsPrefixOfConstraint(node.getURI()));
                    }


                    //varToPrefixes.merge(var, , remappingFunction)
                }
            }
        }
        return result;
        //
//        boolean isUnsatisfiable = false;
//        // Prefix constraints
//        for(int i = 0; i < 4; ++i) {
//            Var var = Vars.gspo.get(i);
//            String columnName = COLUMN_NAMES[i];
//
//
//
//            Node n = QuadUtils.getNode(quad, i);
//
//            /*
//            if(!(n instanceof Var)) {
//                System.out.println("debug");
//            }
//            */
//
//            termRestriction[i] = r;
//
//            if(r.getRdfTermTypes().contains(RdfTermType.URI) && r.hasConstant()) {
//                String columnName = columnNames[i];
//
//                result.put(columnName, new IsPrefixOfConstraint(r.getNode().getURI()));
//            }
//        }
//
//        if(isUnsatisfiable) {
//            return null;
//        }
//
//        // Object type constraint
//        RestrictionImpl r = termRestriction[3];
//        if(r != null) {
//            switch(r.getType()) {
//            case URI:
//                result.put("o_type", new EqualsConstraint(1));
//                break;
//            case LITERAL:
//                result.put("o_type", new EqualsConstraint(2));
//                break;
//            }
//        }
//
//        return result;
    }


    @Override
    public Collection<T> apply(Expr expr) {
        Set<Set<Expr>> dnf = DnfUtils.toSetDnf(expr);

        Set<T> result = new LinkedHashSet<T>();
        for(Set<Expr> clause : dnf) {
            Map<String, Constraint> columnConstraints = inferColumnConstraints(clause);

            // null indicates unsatisfiablity
            if(columnConstraints == null) {
                continue;
            }


            int valueIndex = table.getColumns().size() - 1;
            Collection<List<Object>> rows = table.select(columnConstraints);

            @SuppressWarnings("unchecked")
            List<T> matches = rows.stream()
                    .map(row -> (T)row.get(valueIndex))
                    .collect(Collectors.toList());

            result.addAll(matches);
            // TODO Filter the matches by satisfiability of the expr
        }

        return result;
    }

}
