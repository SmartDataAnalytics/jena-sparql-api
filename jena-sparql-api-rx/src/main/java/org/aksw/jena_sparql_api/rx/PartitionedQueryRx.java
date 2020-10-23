package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.aksw.jena_sparql_api.rx.AggObjectGraph.AccObjectGraph;
import org.aksw.jena_sparql_api.rx.op.OperatorOrderedGroupBy;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.ModelUtils;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

/**
 * Methods for the execution of {@link PartitionedQuery}s.O
 *
 * @author raven
 *
 */
public class PartitionedQueryRx {

    /** Execute a partitioned query.
     * See {@link #execConstructRooted(SparqlQueryConnection, PartitionedQuery, Supplier, ExprListEval)} */
    public static Flowable<RDFNode> execConstructRooted(SparqlQueryConnection conn, PartitionedQuery query) {
        return execConstructRooted(
                conn, query,
                GraphFactory::createDefaultGraph);
    }

    /** Execute a partitioned query.
     * See {@link #execConstructRooted(SparqlQueryConnection, PartitionedQuery, Supplier, ExprListEval)} */
    public static Flowable<RDFNode> execConstructRooted(SparqlQueryConnection conn, PartitionedQuery query,
            Supplier<Graph> graphSupplier) {
        return execConstructRooted(
                conn, query,
                GraphFactory::createDefaultGraph, PartitionedQueryRx::defaultEvalToNode);
    }

    /**
     * Execute a partitioned SPARQL SELECT query such as
     * SELECT ?s ?p ?o { ?s ?p ?o } PARTITION BY ?s
     *
     * All bindings that bind the partition variables to the same values are returned as a group in
     * a {@link Table}.
     *
     * @param conn The connection on which to run the query
     * @param query
     * @return A flowable of pairs of key bindings and tables
     */
    public static Flowable<Entry<Binding, Table>> execSelectPartitioned(
            SparqlQueryConnection conn,
            PartitionedQuery query) {

        Query standardQuery = query.toStandardQuery();

        return execSelectPartitioned(conn, standardQuery, query.getPartitionVars());
    }

    /**
     * Execute a SPARQL select query and partition its result set by the given partition
     * variables.
     *
     * @param conn
     * @param selectQuery
     * @param partitionVars
     * @return
     */
    public static Flowable<Entry<Binding, Table>> execSelectPartitioned(
            SparqlQueryConnection conn,
            Query selectQuery,
            List<Var> partitionVars) {

        if (!selectQuery.isSelectType()) {
            throw new RuntimeException("Query must be of select type");
        }

        Function<Binding, Binding> bindingToKey = SparqlRx.createGrouper(partitionVars, false);

        Aggregator<Binding, Table> aggregator = new AggCollection<>(
                TableFactory::create,
                Function.identity(),
                Table::addBinding
                );

        Flowable<Entry<Binding, Table>> result = SparqlRx
                // For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
                .execSelectRaw(() -> conn.query(selectQuery))
                .compose(aggregateConsecutiveItemsWithSameKey(bindingToKey, aggregator));

        return result;
    }

    /**
     * Execute a CONSTRUCT query using partitions.
     *
     * @param conn
     * @param queryEx
     * @param graphSupplier
     * @param exprListEval
     * @return
     */
    public static Flowable<GraphPartitionWithRoots> execConstructPartitioned(
            SparqlQueryConnection conn,
            PartitionedQuery queryEx,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

        Template template = queryEx.toStandardQuery().getConstructTemplate();
        Map<Node, ExprList> idMapping = queryEx.getIdMapping();

        List<Var> partitionVars = queryEx.getPartitionVars();
        List<SortCondition> partitionOrderBy = queryEx.getPartitionOrderBy();

        Set<Var> essentialProjectVars = getEssentialProjectVars(
                template, queryEx.getIdMapping());

        Node rootNode = queryEx.getRootNode();
//        Function<Binding, Node> bindingToRootNodeInst = rootNode == null
//                ? null
//                : createKeyFunction(rootNode, idMapping, exprListEval);

        Set<Node> trackedTemplateNodes = rootNode == null
                ? Collections.emptySet()
                : Collections.singleton(rootNode);

        Query standardQuery = queryEx.toStandardQuery();

        Set<Var> blacklist = QueryUtils.mentionedVars(standardQuery);
        Generator<Var> varGen = VarGeneratorBlacklist.create("sortKey", blacklist);

        Query selectQuery = preprocessQueryForPartition(
                standardQuery,
                partitionVars,
                essentialProjectVars,
                partitionOrderBy,
                varGen);

        Function<Table, AccObjectGraph> tableToGraph = createTableToGraphMapper(
                template,
                trackedTemplateNodes,
                idMapping,
                exprListEval,
                graphSupplier);

        Flowable<GraphPartitionWithRoots> result = execSelectPartitioned(
                conn, selectQuery, partitionVars)
                .map(keyAndTable -> {
                    Binding key = keyAndTable.getKey();
                    Table table = keyAndTable.getValue();

                    // TODO We need to reuse the blank node map used to instantiate the graph
                    AccObjectGraph acc = tableToGraph.apply(table);
                    Graph graph = acc.getValue();
                    Set<Node> rootNodes =  acc.getTrackedNodes(rootNode);

//                    Set<Node> trackedNodes = acc.getTrackedNodes(rootNode);
//                    if (bindingToRootNodeInst != null) {
//
//                        Iterator<Binding> it = table.rows();
//                        while (it.hasNext()) {
//                            Binding binding = it.next();
//                            Node inst = bindingToRootNodeInst.apply(binding);
//                            rootNodes.add(inst);
//                        }
//                    }

                    GraphPartitionWithRoots r = new GraphPartitionWithRoots(key, graph, rootNodes);
                    return r;
                });

        return result;
    }



    /**
     * Execute a CONSTRUCT query w.r.t. partitions. For every partition a graph fragment is constructed
     * based on bindings that fell into the partition.
     * In addition, designate all values in that partition that were bound to the node referred to by
     * {@link PartitionedQuery#getRootNode()} as 'roots' of that partition.
     * Roots serve as designated starting points for traversal of the graph fragment.
     * Each root is returned as as separate {@link RDFNode} instance that holds a reference
     * to that partition's graph.
     *
     * @param conn
     * @param queryEx
     * @param graphSupplier
     * @param exprListEval
     * @return
     */
    public static Flowable<RDFNode> execConstructRooted(
            SparqlQueryConnection conn,
            PartitionedQuery queryEx,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

        Flowable<RDFNode> result = execConstructPartitioned(conn, queryEx, graphSupplier, exprListEval)
            .flatMap(graphPartition -> Flowable.fromIterable(graphPartition.getRoots())
                    .map(node -> {
                        Graph graph = graphPartition.getGraph();
                        Model model = ModelFactory.createModelForGraph(graph);
                        RDFNode r = ModelUtils.convertGraphNodeToRDFNode(node, model);
                        return r;
                    }));

        return result;
    }

    /**
     * Util function to yield a mapper from tables to graphs based on the provided
     * arguments.
     *
     */
    public static Function<Table, AccObjectGraph> createTableToGraphMapper(
            Template template,
            Set<? extends Node> trackedTemplateNodes,
            Map<Node, ExprList> idMapping,
            ExprListEval exprListEval,
            Supplier<Graph> graphSupplier) {

        AggObjectGraph graphAgg = createGraphAggregator(template, trackedTemplateNodes, idMapping, exprListEval, graphSupplier);

        return table -> {
            AccObjectGraph acc = graphAgg.createAccumulator();
            table.rows().forEachRemaining(acc::accumulate);
            //Graph graph = acc.getValue();
            return acc;
        };
    }


    /**
     * Based on the information present in {@link PartitionedQuery} return a function that
     * deterministically yields the same node (possibly a blank node) when passing equivalent bindings
     * to it.
     *
     * @param root
     * @param idMapping
     * @param exprListEval
     * @return
     */
    public static Function<Binding, Node> createKeyFunction(
            Node root,
            Map<Node, ExprList> idMapping,
            ExprListEval exprListEval) {

        Function<Binding, Node> result;
        if (root.isVariable()) {
            Var rootVar = (Var)root;
            result = b -> b.get(rootVar);
        } else if (root.isBlank()) {
            // The root node must be mapped to ids
            // TODO Currently the limitation is that the mapping must be a list of vars rather than arbitrary expressions
            ExprList el = idMapping.get(root);
            Objects.requireNonNull(el, "blank node as the root must be mapped to id-generating expressions");

            result = b -> exprListEval.eval(el, b);
        } else {
            // Case where the root node is a constant;
            // unlikely to be useful but handled for completeness
            result = b -> root;
        }

        return result;
    }


    /**
     * Create an aggregator whose accumulators accumulate graphs from Bindings
     * w.r.t. to the provided mapping information.
     *
     * @param template
     * @param idMap
     * @param exprListEval
     * @param graphSupplier
     * @return
     */
    public static AggObjectGraph createGraphAggregator(
            Template template,
            Set<? extends Node> trackedTemplateNodes,
            Map<Node, ExprList> idMap,
            ExprListEval exprListEval,
            Supplier<Graph> graphSupplier) {

        Map<Node, Function<Binding, Node>> nodeIdGenMap = idMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> (binding -> exprListEval.eval(e.getValue(), binding))));

        AggObjectGraph result = new AggObjectGraph(
                template,
                trackedTemplateNodes,
                graphSupplier,
                nodeIdGenMap);

        return result;
    }


    /**
     * One of the many ways to create always the same node (equals)
     * from the values obtained by evaluating a list of expressions w.r.t.
     * a given binding.
     *
     * @param exprs
     * @param binding
     * @return
     */
    public static Node defaultEvalToNode(ExprList exprs, Binding binding) {
        List<Node> nodes = exprs.getList().stream()
                .map(expr -> ExprUtils.eval(expr, binding))
                .map(NodeValue::asNode)
                .collect(Collectors.toList());

        String label = nodes.toString();

        Node result = NodeFactory.createBlankNode(label);
        return result;
    }


    /**
     * A generic flowable transformer that groups consecutive items that evaluate to the same key.
     * For every group an accumulator is created that receives the items.
     *
     * @param <ITEM> The incoming item type
     * @param <KEY> The type of the keys derived from the items
     * @param <VALUE> The type of the value accumulated from the items
     * @param itemToKey A function that yiels an item's key
     * @param aggregator An aggregator for computing a value from the set of items with the same key
     * @return
     */
    public static <ITEM, KEY, VALUE> FlowableTransformer<ITEM, Entry<KEY, VALUE>> aggregateConsecutiveItemsWithSameKey(
            Function<? super ITEM, KEY> itemToKey,
            Aggregator<? super ITEM, VALUE> aggregator) {
        return upstream -> upstream
            .lift(OperatorOrderedGroupBy.<ITEM, KEY, Accumulator<? super ITEM, VALUE>>create(
                    itemToKey,
                    groupKey -> aggregator.createAccumulator(),
                    Accumulator::accumulate))
            .map(keyAndAcc -> {
                KEY groupKey = keyAndAcc.getKey();
                Accumulator<? super ITEM, VALUE> accGraph = keyAndAcc.getValue();

                VALUE g = accGraph.getValue();
                return Maps.immutableEntry(groupKey, g);
            });
    }


    /**
     * Return the sets of variables used in the template and the id mapping.
     *
     *
     * @param objectQuery
     * @return
     */
//    public static Set<Var> getRequiredVars(ObjectQuery query) {
//        return getEssentialProjectVars(query.getTemplate(), query.getIdMapping());
//    }

//    public static Set<Var> getRequiredVars(QueryEx query) {
//        return getEssentialProjectVars(query.getConstructTemplate(), query.getIdMapping());
//    }

    public static Set<Var> getEssentialProjectVars(Template template, Map<Node, ExprList> idMapping) {
        Set<Var> result = new LinkedHashSet<>();

        for (ExprList exprs : idMapping.values()) {
            ExprVars.varsMentioned(result, exprs);
        }

        result.addAll(QuadPatternUtils.getVarsMentioned(template.getQuads()));

        return result;
    }

    /**
     * Prepend a given sequence of sort conditions to those
     * already in the query (if there are already any).
     * Duplicate sort conditions are removed in the process
     *
     * @param query
     * @param sortConditions The sort conditions. If null or empty this method becomes a no-op.
     * @return The input query
     */
    public static Query prependToOrderBy(Query query, List<SortCondition> sortConditions) {
        if (sortConditions != null && !sortConditions.isEmpty()) {
            Stream<SortCondition> newConditions;

            if (query.hasOrderBy()) {
                // We need to make a copy using Sets.newLinkedHashSet because we are going to change query.getOrderBy()
                newConditions = Sets.newLinkedHashSet(Iterables.concat(sortConditions, query.getOrderBy())).stream();
                query.getOrderBy().clear();
            } else {
                newConditions = sortConditions.stream();
            }

            newConditions.forEach(query::addOrderBy);
        }

        return query;
    }

    /** Create sort conditions with the given directions from an iterable of {@link Expr}s */
    public static List<SortCondition> createSortConditionsFromExprs(Iterable<Expr> exprs, int dir) {
        List<SortCondition> result = exprs == null
                ? null
                : Streams.stream(exprs)
                    .map(expr -> new SortCondition(expr, dir))
                    .collect(Collectors.toList());
        return result;
    }


    /** Util function to create sort conditions from variables and a direction */
    public static List<SortCondition> createSortConditionsFromVars(Iterable<Var> vars, int dir) {
        List<SortCondition> result = vars == null
                ? null
                : Streams.stream(vars)
                    .map(var -> new SortCondition(new ExprVar(var), dir))
                    .collect(Collectors.toList());
        return result;
    }


    /**
     * Return a SELECT query from the given query where
     * - it is ensured that all partitionVars are part of the projection (if they aren't already)
     * - distinct is applied in preparation to instantiation of construct templates (where duplicates can be ignored)
     * - if sortRowsByPartitionVar is true then result bindings are sorted by the primary key vars
     *   so that bindings that belong together are consecutive
     * - In case of a construct template without variables variable free is handled
     *
     * @param baseQuery
     * @param partitionVars
     * @param requiredVars The variables that need to be projected in the resulting query
     * @param sortRowsByPartitionVar
     * @return
     */
    public static Query preprocessQueryForPartition(
            Query baseQuery,
            List<Var> partitionVars,
            Set<Var> requiredVars,
            List<SortCondition> partitionOrderBy,
            Generator<Var> varGenerator) {

        Query result = preprocessQueryForPartitionWithoutOrder(
                baseQuery,
                partitionVars,
                requiredVars,
                true);

        partitionOrderBy = partitionOrderBy == null
                ? Collections.emptyList()
                : partitionOrderBy;

        // Allocate variables for each sort condition
        List<Var> sortKeyVars = partitionOrderBy.stream()
                .map(x -> varGenerator.next())
                .collect(Collectors.toList());

        Element basePattern = result.getQueryPattern();

        Query subSelect = new Query();
        subSelect.setQuerySelectType();
        subSelect.setQueryPattern(basePattern);

        for (Var partitionVar : partitionVars) {
            subSelect.addResultVar(partitionVar);
            subSelect.addGroupBy(partitionVar);
        }

        for (int i = 0; i < partitionOrderBy.size(); ++i) {
            SortCondition sc = partitionOrderBy.get(i);
            Var scv = sortKeyVars.get(i);

            // TODO The sort condition will contain an aggregate function
            // that must be allocated on the query
            //subSelect.allocAggregate(agg)
            Expr rawExpr = sc.getExpression();
            Expr expr = ExprTransformer.transform(new ExprTransformAllocAggregate(subSelect), rawExpr);
            subSelect.addResultVar(scv, expr);
        }

        ElementGroup newPattern = ElementUtils.createElementGroup(new ElementSubQuery(subSelect));
        ElementUtils.copyElements(newPattern, basePattern);

        // Update the query pattern
        result.setQueryPattern(newPattern);


        // Prepend the sort conditions
        List<SortCondition> partitionScs = new ArrayList<>();
        for (int i = 0; i < partitionOrderBy.size(); ++i) {
            SortCondition sc = partitionOrderBy.get(i);
            Var scv = sortKeyVars.get(i);

            partitionScs.add(new SortCondition(scv, sc.getDirection()));
        }
        prependToOrderBy(result, partitionScs);



        System.out.println(result);

        return result;
    }


    /**
     *
     * @param baseQuery
     * @param partitionVars
     * @param requiredVars
     * @param sortRowsByPartitionVars
     * @return
     */
    public static Query preprocessQueryForPartitionWithoutOrder(
            Query baseQuery,
            List<Var> partitionVars,
            Set<Var> requiredVars,
            boolean sortRowsByPartitionVars) {

        Query selectQuery = baseQuery.cloneQuery();
        selectQuery.setQuerySelectType();
        selectQuery.setQueryResultStar(false);

        VarExprList project = selectQuery.getProject();

        VarExprListUtils.addAbsentVars(project, partitionVars);
        VarExprListUtils.addAbsentVars(project, requiredVars);

        // Handle the corner case where no variables are requested
        if (project.isEmpty()) {
            // If the template is variable free then project the first variable of the query pattern
            // If the query pattern is variable free then just use the result star
            Set<Var> patternVars = SetUtils.asSet(PatternVars.vars(selectQuery.getQueryPattern()));
            if(patternVars.isEmpty()) {
                selectQuery.setQueryResultStar(true);
            } else {
                Var v = patternVars.iterator().next();
                selectQuery.setQueryResultStar(false);
                selectQuery.getProject().add(v);
            }
        }

        selectQuery.setDistinct(true);

        if (sortRowsByPartitionVars) {
            List<SortCondition> newSortConditions = createSortConditionsFromVars(partitionVars, Query.ORDER_DEFAULT);
            prependToOrderBy(selectQuery, newSortConditions);
        }

        return selectQuery;
    }
}


//    public static Flowable<RDFNode> execConstructRooted(
//            SparqlQueryConnection conn,
//            RootedQuery rootedQuery,
//            Supplier<Graph> graphSupplier,
//            ExprListEval evalFn) {
//
//        ObjectQuery objectQuery = rootedQuery.getObjectQuery();
//
//        Node root = rootedQuery.getRootNode();
//
//        Query selectQuery = objectQuery.getRelation().toQuery();
//        Set<Var> requiredVars = getRequiredVars(objectQuery);
//
//        List<Var> partitionVars;
//        Function<Binding, Node> keyToNode;
//
//        if (root.isVariable()) {
//            Var rootVar = (Var)root;
//            partitionVars = Collections.singletonList(rootVar);
//            // pkExprs = new ExprList(new ExprVar(rootVar));
//            keyToNode = b -> b.get(rootVar);
//        } else if (root.isBlank()) {
//            // The root node must be mapped to ids
//            // TODO Currently the limitation is that the mapping must be a list of vars rather than arbitrary expressions
//            ExprList el = objectQuery.getIdMapping().get(root);
//            Objects.requireNonNull(el, "blank node as the root must be mapped to id-generating expressions");
//
//            partitionVars = el.getListRaw().stream()
//                    .map(ExprVars::getVarsMentioned)
//                    .flatMap(Collection::stream)
//                    .distinct()
//                    .collect(Collectors.toList());
//
//            keyToNode = b -> evalFn.eval(el, b);
//        } else {
//            // Case where the root node is a constant;
//            // unlikely to be useful but handled for completeness
//            partitionVars = Collections.emptyList();
//            keyToNode = b -> root;
//        }
//
//        Query clone = preprocessQueryForPartition(selectQuery, partitionVars, requiredVars, true);
//
//        Aggregator<Binding, Graph> agg = createGraphAggregator(objectQuery, evalFn, graphSupplier);
//
//        Flowable<RDFNode> result = execConstructGrouped(conn::query, agg, clone, partitionVars)
//                .map(e -> {
//                    Binding b = e.getKey();
//                    Graph g = e.getValue();
//
//                    Node rootNode = keyToNode.apply(b);
//                    Model m = ModelFactory.createModelForGraph(g);
//
//                    RDFNode r = ModelUtils.convertGraphNodeToRDFNode(rootNode, m);
//                    return r;
//                });
//
//        return result;
//    }

//public static Flowable<RDFNode> execConstructRooted(
//SparqlQueryConnection conn,
//RootedQuery rootedQuery,
//Supplier<Graph> graphSupplier,
//ExprListEval evalFn) {
//
//ObjectQuery objectQuery = rootedQuery.getObjectQuery();
//
//Node root = rootedQuery.getRootNode();
//
//Query selectQuery = objectQuery.getRelation().toQuery();
//Set<Var> requiredVars = getRequiredVars(objectQuery);
//
//List<Var> partitionVars;
//Function<Binding, Node> keyToNode;
//
//if (root.isVariable()) {
//Var rootVar = (Var)root;
//partitionVars = Collections.singletonList(rootVar);
//// pkExprs = new ExprList(new ExprVar(rootVar));
//keyToNode = b -> b.get(rootVar);
//} else if (root.isBlank()) {
//// The root node must be mapped to ids
//// TODO Currently the limitation is that the mapping must be a list of vars rather than arbitrary expressions
//ExprList el = objectQuery.getIdMapping().get(root);
//Objects.requireNonNull(el, "blank node as the root must be mapped to id-generating expressions");
//
//partitionVars = el.getListRaw().stream()
//      .map(ExprVars::getVarsMentioned)
//      .flatMap(Collection::stream)
//      .distinct()
//      .collect(Collectors.toList());
//
//keyToNode = b -> evalFn.eval(el, b);
//} else {
//// Case where the root node is a constant;
//// unlikely to be useful but handled for completeness
//partitionVars = Collections.emptyList();
//keyToNode = b -> root;
//}
//
//Query clone = preprocessQueryForPartition(selectQuery, partitionVars, requiredVars, true);
//
//Aggregator<Binding, Graph> agg = createGraphAggregator(objectQuery, evalFn, graphSupplier);
//
//Flowable<RDFNode> result = execConstructGrouped(conn::query, agg, clone, partitionVars)
//  .map(e -> {
//      Binding b = e.getKey();
//      Graph g = e.getValue();
//
//      Node rootNode = keyToNode.apply(b);
//      Model m = ModelFactory.createModelForGraph(g);
//
//      RDFNode r = ModelUtils.convertGraphNodeToRDFNode(rootNode, m);
//      return r;
//  });
//
//return result;
//}

//public static Flowable<RDFNode> execConstructRooted(
//SparqlQueryConnection conn,
//RootedQuery rootedQuery) {
//return execConstructRooted(conn, rootedQuery, GraphFactory::createDefaultGraph);
//}
//
//public static Flowable<RDFNode> execConstructRooted(
//SparqlQueryConnection conn,
//RootedQuery rootedQuery,
//Supplier<Graph> graphSupplier) {
//return execConstructRooted(conn, rootedQuery, GraphFactory::createDefaultGraph, RootedQueryRx::evalToNode);
//}
//

//public static FlowableTransformer<Entry<Binding, Table>, Entry<Binding, Graph>> graphsFromPartitions(
//        Template template,
//        Map<Node, ExprList> idMapping,
//        ExprListEval exprListEval,
//        Supplier<Graph> graphSupplier) {
//
//    Aggregator<Binding, Graph> graphAgg = createGraphAggregator(template, idMapping, exprListEval, graphSupplier);
//
//    return upstream ->
//        upstream.map(keyAndTable -> {
//            Accumulator<Binding, Graph> acc = graphAgg.createAccumulator();
//
//            Table table = keyAndTable.getValue();
//            table.rows().forEachRemaining(acc::accumulate);
//
//            Graph graph = acc.getValue();
//
//            return Maps.immutableEntry(keyAndTable.getKey(), graph);
//        });
//}


//public static <T> Flowable<Entry<Binding, T>> execConstructGrouped(
//      Function<Query, QueryExecution> qeSupp,
//      Aggregator<? super Binding, T> aggregator,
//      Query clone,
//      List<Var> primaryKeyVars) {
//
//  Function<Binding, Binding> grouper = SparqlRx.createGrouper(primaryKeyVars, false);
//
//  Flowable<Entry<Binding, T>> result = SparqlRx
//      // For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
//      .execSelectRaw(() -> qeSupp.apply(clone))
//      .compose(aggregateConsecutiveItemsWithSameKey(grouper, aggregator));
//
//  return result;
//}


//public static Query appendToProject(Query query, List<Var> vars) {
//	query.addProjectVars(vars);
//}
//
//public static Query appendToProject(Query query, VarExprList vel) {
//
//}