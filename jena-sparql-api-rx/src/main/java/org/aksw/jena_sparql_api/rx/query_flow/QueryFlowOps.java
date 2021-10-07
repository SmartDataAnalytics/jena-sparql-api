package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.rx.util.FlowableEx;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.binding.BindingProject;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.base.Preconditions;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;

public class QueryFlowOps
{

    /** Create a mapper for a construct query yielding triples (similar to tarql) */
    public static FlowableTransformer<Binding, Triple> createMapperTriples(Query query) {
        Preconditions.checkArgument(!query.isConstructType(), "Construct query expected");

        Template template = query.getConstructTemplate();
        Op op = Algebra.compile(query);

        return upstream ->
            upstream
                .compose(createMapperBindings(op))
                .flatMap(createMapperTriples(template)::apply);
    }


    /** Create a mapper for a construct query yielding quads (similar to tarql) */
    public static FlowableTransformer<Binding, Quad> createMapperQuads(Query query) {
        Preconditions.checkArgument(query.isConstructType(), "Construct query expected");

        Template template = query.getConstructTemplate();
        Op op = Algebra.compile(query);

        return upstream ->
            upstream
                .compose(createMapperBindings(op))
                .flatMap(createMapperQuads(template)::apply);
    }


    public static FlowableTransformer<Binding, Binding> createMapperBindings(Op op) {
        return upstream -> {
            DatasetGraph ds = DatasetGraphFactory.create();
            Context cxt = ARQ.getContext().copy();
            ExecutionContext execCxt = new ExecutionContext(cxt, ds.getDefaultGraph(), ds, QC.getFactory(cxt));

            return upstream.flatMap(binding -> FlowableEx.fromIteratorSupplier(
                    () -> QC.execute(op, binding, execCxt), QueryIterator::close));
        };
    }


    public static Function<Binding, Flowable<Triple>> createMapperTriples(Template template) {
        return binding -> Flowable.fromIterable(
                    () -> TemplateLib.calcTriples(template.getTriples(), Collections.singleton(binding).iterator()));
    }

    /**
     *
     * Usage
     *   Flowable<Quad> quads = flowOfBindings.concatMap(createMapperQuads(template)::apply);
     *
     *
     * @param template
     * @return
     */
    public static Function<Binding, Flowable<Quad>> createMapperQuads(Template template) {
        return binding -> Flowable.fromIterable(
                    () -> TemplateLib.calcQuads(template.getQuads(), Collections.singleton(binding).iterator()));
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
    public static Predicate<Binding> createFilter(Expr expr, FunctionEnv execCxt) {
        return binding -> expr.isSatisfied(binding, execCxt);
    }

    public static Predicate<Binding> createFilter(ExprList exprs, FunctionEnv execCxt) {
        return binding -> {
            boolean r = true;
            for (Expr expr : exprs) {
                r = expr.isSatisfied(binding, execCxt);
                if (!r) {
                    break;
                }
            }
            return r;
        };
    }

    public static Predicate<Binding> createFilter(String exprStr, FunctionEnv execCxt) {
        Expr expr = ExprUtils.parse(exprStr);
        return createFilter(expr, execCxt);
    }

    public static Predicate<Binding> createFilter(String exprStr, PrefixMapping pm, FunctionEnv execCxt) {
        Expr expr = ExprUtils.parse(exprStr, pm);
        return createFilter(expr, execCxt);
    }

    public static FlowableTransformer<Binding, Binding> createAssign(
            VarExprList exprs,
            FunctionEnv execCxt) {
        return upstream -> upstream.map(binding -> QueryFlowAssign.assign(binding, exprs, execCxt));
    }


    public static FlowableTransformer<Binding, Binding> createProject(
            Collection<Var> vars,
            FunctionEnv execCxt) {
        return upstream -> upstream.map(binding -> new BindingProject(vars, binding));
    }

    public static <T> FlowableTransformer<T, T> createSlice(Long offset, Long limit) {
        return upstream -> {
            if (offset != null && offset != Query.NOLIMIT) {
                upstream = upstream.skip(offset);
            }

            if (limit != null && limit != Query.NOLIMIT) {
                upstream = upstream.take(limit);
            }

            return upstream;
        };
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
    public static FlowableTransformer<Binding, Binding> createTransformForGroupBy(Query query, FunctionEnv execCxt) {
        VarExprList groupVarExpr = query.getGroupBy();
        List<ExprAggregator> aggregators = query.getAggregators();

        // Create an updated projection
        VarExprList rawProj = query.getProject();


        VarExprList vel = null;
        if (!query.isQueryResultStar()) {
            vel = new VarExprList();
            for (Var v : rawProj.getVars() )
            {
                Expr e = rawProj.getExpr(v) ;
                if ( e != null )
                {
                    Expr e2 = ExprLib.replaceAggregateByVariable(e) ;
                    vel.add(v, e2) ;
                } else {
                    vel.add(v);
                }
                // Include in project
                // vars.add(v) ;
            }
        }

        VarExprList finalVel = vel;

        List<SortCondition> newScs = query.getOrderBy() == null ? Collections.emptyList() : query.getOrderBy().stream()
                .map(sc -> new SortCondition(ExprLib.replaceAggregateByVariable(sc.getExpression()), sc.getDirection()))
                .collect(Collectors.toList());

//        Op op = Algebra.compile(query);
//        System.out.println(op);

        FlowableTransformer<Binding, Binding> result = upstream -> {
            Flowable<Binding> r = upstream;

            if (!aggregators.isEmpty()) {
                r = r.compose(QueryFlowGroupBy.createTransformer(execCxt, groupVarExpr, aggregators));
            }

            if (finalVel != null) {
                r = r.compose(createAssign(finalVel, execCxt));
            }

            if (!newScs.isEmpty()) {
                r = r.compose(createOrderBy(newScs));
            }

            if (finalVel != null) {
                r = r.compose(createProject(finalVel.getVars(), execCxt));
            }

            r = r.compose(createSlice(query.getOffset(), query.getLimit()));

            return r;
        };
        return result;
    }


    public static FlowableTransformer<Binding, Binding> createOrderBy(List<SortCondition> sortConditions) {
        Comparator<Binding> bindingCmp = new BindingComparator(sortConditions);

        return sortConditions.isEmpty()
                ? upstream -> upstream
                : upstream -> upstream.sorted(bindingCmp);
    }


}