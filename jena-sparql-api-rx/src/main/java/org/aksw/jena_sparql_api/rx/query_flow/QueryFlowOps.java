package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;

public class QueryFlowOps
{
    public static Function<Binding, Flowable<Triple>> createMapperTriples(Template template) {
        return binding -> Flowable.fromIterable(
                    () -> TemplateLib.calcTriples(template.getTriples(), Collections.singleton(binding).iterator()));
    }


    /**
     * Utility method to set up a default execution context
     *
     * @return
     */
    public static ExecutionContext createExecutionContextDefault() {
        Context context = ARQ.getContext().copy();
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
        ExecutionContext result = new ExecutionContext(context, null, null, null);
        return result;
    }


    /**
     * Convenience method with default execution context.
     *
     * See {@link #createTransformForGroupBy(Query, ExecutionContext)}.
     *
     * @param query
     * @return
     */
    public static FlowableTransformer<Binding, Binding> transformerFromQuery(String queryStr) {
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
        FlowableTransformer<Binding, Binding> result = QueryFlowOps.transformerFromQuery(query);
        return result;
    }


    /**
     * Convenience method with default execution context.
     *
     * See {@link #createTransformForGroupBy(Query, ExecutionContext)}.
     *
     * @param query
     * @return
     */
    public static FlowableTransformer<Binding, Binding> transformerFromQuery(Query query) {
        ExecutionContext execCxt = createExecutionContextDefault();
        FlowableTransformer<Binding, Binding> result = QueryFlowOps.createTransformForGroupBy(query, execCxt);
        return result;
    }


    /**
     * Wrap a supplier of {@link ClosableIterator} (base of ExtendedIterator) as a Flowable.
     *
     * @param <T>
     * @param itSupp
     * @return
     */
    public static <T> Flowable<T> wrapClosableIteratorSupplier(Supplier<? extends ClosableIterator<T>> itSupp) {
        return Flowable.generate(
                () -> itSupp.get(),
                (it, emitter) -> {
                    if(it.hasNext()) {
                        T item = it.next();
                        emitter.onNext(item);
                    } else {
//                        System.out.println("Emitting completing event");
                        emitter.onComplete();
                    }
                },
                ClosableIterator::close);
    }

    public static Flowable<Triple> createFlowableFromGraph(Graph g, Triple pattern) {
        return wrapClosableIteratorSupplier(() -> {
            ExtendedIterator<Triple> r = g.find(pattern);
            return r;
        });
    }


    /**
     * Create a mapper that for each binding performs a join using a lookup using the given graph and triple pattern.
     * Usage: flowableOfBindings.flatMap(createMapper(g, tp))
     *
     * @param graph
     * @param triplePattern
     * @return
     */
    public static Function<Binding, Flowable<Binding>> createMapperForJoin(Graph graph, Triple triplePattern) {
        return binding -> {
            Triple substPattern = Substitute.substitute(triplePattern, binding);
            Triple findPattern = varToAny(substPattern);

            return
                wrapClosableIteratorSupplier(() -> {
                    ExtendedIterator<Triple> r = graph.find(findPattern);
                    return r;
                })
                .flatMapMaybe(contrib -> {

                    BindingMap map = BindingFactory.create(binding);
                    Binding r = mapper(map, substPattern, contrib);

                    return r == null ? Maybe.empty() : Maybe.just(r);
                });
        };
    }

    public static Function<Binding, Flowable<Binding>> createMapperForOptionalJoin(Graph graph, Triple triplePattern) {
        return binding -> {
            Triple substPattern = Substitute.substitute(triplePattern, binding);
            Triple findPattern = varToAny(substPattern);

            return
                wrapClosableIteratorSupplier(() -> {
                    ExtendedIterator<Triple> r = graph.find(findPattern);
                    return r;
                })
                .flatMapMaybe(contrib -> {

                    BindingMap map = BindingFactory.create(binding);
                    Binding r = mapper(map, substPattern, contrib);

                    return r == null ? Maybe.empty() : Maybe.just(r);
                })
                .defaultIfEmpty(binding);
        };
    }

    public static Node varToAny(Node node)
    {
        if ( node.isVariable() )
            return Node.ANY ;
        return node ;
    }

    public static Triple varToAny(Triple tp) {
        Triple result = Triple.create(
                varToAny(tp.getSubject()),
                varToAny(tp.getPredicate()),
                varToAny(tp.getObject()));
        return result;
    }

    /**
     * Utility method used by {@link createMapperForJoin}
     *
     * @param result
     * @param pattern
     * @param match
     * @return
     */
    public static Binding mapper(BindingMap result, Triple pattern, Triple match)
    {
        if (!insert(pattern.getMatchSubject(), match.getSubject(), result)) {
            return null;
        }

        if (!insert(pattern.getMatchPredicate(), match.getPredicate(), result)) {
            return null;
        }

        if (!insert(pattern.getMatchObject(), match.getObject(), result)) {
            return null;
        }

        return result;
    }

    public static boolean insert(Node inputNode, Node outputNode, BindingMap results)
    {
        if ( ! Var.isVar(inputNode) )
            return true ;

        Var v = Var.alloc(inputNode) ;
        Node x = results.get(v) ;
        if ( x != null )
            return outputNode.equals(x) ;

        results.add(v, outputNode) ;
        return true ;
    }

    /**
     *
     * Usage: flowableOfBindings.filter(createFilter(execCxt, expr))
     *
     * @param execCxt
     * @param expr
     * @return
     */
    public static Predicate<Binding> createFilter(ExecutionContext execCxt, Expr expr) {
        return binding -> expr.isSatisfied(binding, execCxt);
    }

    public static Predicate<Binding> createFilter(ExecutionContext execCxt, String exprStr) {
        Expr expr = ExprUtils.parse(exprStr);
        return createFilter(execCxt, expr);
    }

    /**
     * Create a transformer that implements a group by operation based on the query
     * thereby ignoring its query pattern. Instead of executing a query pattern,
     * the bindings supplied by the upstream flow will be accumulated.
     *
     * Usage: flowableOfBindings.compose(createTransformForGroupBy(query, execCxt))
     *
     *
     * @param query
     * @param execCxt
     * @return
     */
    public static FlowableTransformer<Binding, Binding> createTransformForGroupBy(Query query, ExecutionContext execCxt) {
        VarExprList groupVarExpr = query.getGroupBy();
        List<ExprAggregator> aggregators = query.getAggregators();

        // Create an updated projection
        VarExprList rawProj = query.getProject();


        VarExprList exprs = new VarExprList();
        for (Var v : rawProj.getVars() )
        {
            Expr e = rawProj.getExpr(v) ;
            if ( e != null )
            {
                Expr e2 = ExprLib.replaceAggregateByVariable(e) ;
                exprs.add(v, e2) ;
            } else {
                exprs.add(v);
            }
            // Include in project
            // vars.add(v) ;
        }

//        Op op = Algebra.compile(query);
//        System.out.println(op);

        FlowableTransformer<Binding, Binding> result = upstream -> upstream
                // Do not apply group by if there are no aggregators
                .compose(x -> aggregators.isEmpty() ? x : QueryFlowGroupBy.createTransformer(execCxt, groupVarExpr, aggregators).apply(x))
                .compose(QueryFlowAssign.createTransformer(execCxt, exprs))
                .compose(QueryFlowProject.createTransformer(execCxt, exprs.getVars()));

        return result;
    }



}