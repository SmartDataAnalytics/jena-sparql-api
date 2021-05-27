package org.aksw.jena_sparql_api.rx.entity.engine;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.aksw.jena_sparql_api.rx.AggCollection;
import org.aksw.jena_sparql_api.rx.AggObjectGraph;
import org.aksw.jena_sparql_api.rx.AggObjectGraph.AccObjectGraph;
import org.aksw.jena_sparql_api.rx.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.ExprTransformAllocAggregate;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryBasic;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplate;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.ExprListEval;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionWithEntities;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.MultimapBuilder;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.ext.com.google.common.io.BaseEncoding;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
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
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.util.iterator.ExtendedIterator;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

/**
 * Methods for the execution of {@link EntityQueryBasic}s
 *
 * @author raven
 *
 */
public class EntityQueryRx {


    public static Flowable<Quad> execConstructEntitiesNg(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryImpl query) {

        return execConstructEntitiesNg(
                conn, query,
                GraphFactory::createDefaultGraph);
    }

    public static Flowable<Quad> execConstructEntitiesNg(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryImpl query,
            Supplier<Graph> graphSupplier) {

        return execConstructEntitiesNg(
                conn, query,
                GraphFactory::createDefaultGraph, EntityQueryRx::defaultEvalToNode);
    }

    public static Flowable<Quad> execConstructEntitiesNg(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryImpl queryEx,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

        EntityQueryBasic basicEntityQuery = assembleEntityAndAttributeParts(queryEx);
        return execConstructEntitiesNg(conn, basicEntityQuery, graphSupplier, exprListEval);
        //return execConstructPartitionedOld(conn, assembledQuery, graphSupplier, exprListEval);
    }




    /** Execute a partitioned query.
     * See {@link #execConstructEntities(SparqlQueryConnection, EntityQueryBasic, Supplier, ExprListEval)} */
    public static Flowable<RDFNode> execConstructRooted(
    		Function<? super Query, ? extends QueryExecution> conn, 
    		EntityQueryBasic query) {
        return execConstructRooted(
                conn, query,
                GraphFactory::createDefaultGraph);
    }

    /** Execute a partitioned query.
     * See {@link #execConstructEntities(SparqlQueryConnection, EntityQueryBasic, Supplier, ExprListEval)} */
    public static Flowable<RDFNode> execConstructRooted(
    		Function<? super Query, ? extends QueryExecution> conn,
    		EntityQueryBasic query,
            Supplier<Graph> graphSupplier) {
        return execConstructEntities(
                conn, query,
                GraphFactory::createDefaultGraph, EntityQueryRx::defaultEvalToNode);
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
//    public static Flowable<Entry<Binding, Table>> execSelectPartitioned(
//            SparqlQueryConnection conn,
//            EntityQueryBasic query) {
//
//        Query standardQuery = query.getPartitionSelectorQuery();
//
//        return execSelectPartitioned(conn, standardQuery, query.getPartitionVars());
//    }

    /**
     * Create a transformed copy of the query where all variables that
     * need to join have the same name whereas variables that are not
     * supposed to join have been remapped
     *
     *
     * @param query
     * @return
     */
    public static EntityQueryImpl alignVariables(EntityQueryImpl query) {
        EntityQueryImpl result = new EntityQueryImpl();
        result.setBaseQuery(query.getBaseQuery());

        Set<Var> sourceVars = QueryUtils.mentionedVars(result.getBaseQuery().getStandardQuery());
        List<Var> sourceJoinVars = result.getBaseQuery().getPartitionVars();

        Generator<Var> varGen = VarGeneratorImpl2.create();

        for (GraphPartitionJoin join : query.getMandatoryJoins()) {

            EntityGraphFragment egm = join.getEntityGraphFragment();

            Set<Var> targetVars = ElementUtils.getVarsMentioned(egm.getElement());
            List<Var> targetJoinVars = egm.getPartitionVars();

            // Create a var mapping that joins on the partition vars without
            // causing a clash on any other var
            Map<Var, Var> varMap = VarUtils.createJoinVarMap(
                    sourceVars, targetVars, sourceJoinVars, targetJoinVars, varGen);

            // Add any newly allocated variables in the varMap to the source vars for the next iteration
            // as to prevent accidental joins on the already encountered variables
            sourceVars.addAll(varMap.values());

            NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);
            GraphPartitionJoin newJoin = join.applyNodeTransform(nodeTransform);

            result.getMandatoryJoins().add(newJoin);
        }

        // TODO Get rid of code duplication

        for (GraphPartitionJoin join : query.getOptionalJoins()) {

            EntityGraphFragment egm = join.getEntityGraphFragment();

            Set<Var> targetVars = ElementUtils.getVarsMentioned(egm.getElement());
            List<Var> targetJoinVars = egm.getPartitionVars();

            // Create a var mapping that joins on the partition vars without
            // causing a clash on any other var
            Map<Var, Var> varMap = VarUtils.createJoinVarMap(
                    sourceVars, targetVars, sourceJoinVars, targetJoinVars, varGen);

            // Add any newly allocated variables in the varMap to the source vars for the next iteration
            // as to prevent accidental joins on the already encountered variables
            sourceVars.addAll(varMap.values());

            NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);
            GraphPartitionJoin newJoin = join.applyNodeTransform(nodeTransform);

            result.getOptionalJoins().add(newJoin);
        }
        return result;
    }

    public static EntityQueryBasic assembleEntityAndAttributeParts(EntityQueryImpl queryRaw) {

        EntityQueryImpl queryTmp = alignVariables(queryRaw);
        EntityQueryImpl query = mergeFetchGroups(queryTmp);

//        Query baseQuery = query.getBaseQuery().getStandardQuery();
//        List<Var> partitionVars = query.getBaseQuery().getPartitionVars();

//        List<SortCondition> partitionOrderBy = queryRaw.getBaseQuery().getPartitionOrderBy();

//        boolean needsSubSelect = !(partitionOrderBy == null || partitionOrderBy.isEmpty())
//                || baseQuery.hasLimit()
//                || baseQuery.hasOffset();

//        List<Element> combinedFilter = new ArrayList<>();
//        //List<Element> combinedAttributes = new ArrayList<>();
//        combinedFilter.add(baseQuery.getQueryPattern());
//
//        for (GraphPartitionJoin gp : query.getAuxiliaryGraphPartitions()) {
//            Element elt = gp.getEntityGraphFragment().getElement();
//            if (!(elt instanceof ElementOptional)) {
//                // Make the element join with the partition variables
//                combinedFilter.add(elt);
//            }
//        }
//
//        System.out.println("Filter " + combinedFilter);
//
//
//        for (GraphPartitionJoin gp : query.getAuxiliaryGraphPartitions()) {
//            String lfgn = gp.getLazyFetchGroupName();
////            System.out.println("Fetch group: " + lfgn);
////            System.out.println("CONSTRUCT " + gp.getEntityTemplate().getTemplate().getBGP());
////            System.out.println("WHERE " + gp.getElement());
//            System.out.println("----");
//        }

        GraphPartitionJoin join = Iterables.getFirst(query.getMandatoryJoins(),
                new GraphPartitionJoin(
                        EntityGraphFragment.empty(query.getBaseQuery().getPartitionVars())));

        GraphPartitionJoin optional = Iterables.getFirst(query.getOptionalJoins(),
                new GraphPartitionJoin(
                        EntityGraphFragment.empty(query.getBaseQuery().getPartitionVars())));

        EntityQueryBasic result = new EntityQueryBasic();
        result.setBaseQuery(queryRaw.getBaseQuery());
        result.setAttributeFragment(join.getEntityGraphFragment());
        result.setOptionalAttributeFragment(optional.getEntityGraphFragment());


//        if (true) throw new RuntimeException("implement me");

        return result;
    }


    /**
     * Merges all graph partitions with the same fetch group name into a single
     * graph partition.
     *
     * @param queryRaw
     * @return
     */
    public static EntityQueryImpl mergeFetchGroups(EntityQueryImpl query) {

        Query baseQuery = query.getBaseQuery().getStandardQuery();
        List<Var> partitionVars = query.getBaseQuery().getPartitionVars();

        // First group all graph partitions by name
        // Then merge their templates and patterns into a single one
        Multimap<String, GraphPartitionJoin> rawFetchGroups = MultimapBuilder.hashKeys().arrayListValues().build();
        Multimap<String, GraphPartitionJoin> rawOptionalFetchGroups = MultimapBuilder.hashKeys().arrayListValues().build();

        // todo add the direct graph partition to the fetch group if a subSelect is needed
        // query.getDirectGraphPartition()


        List<Element> combinedFilter = new ArrayList<>();
        //List<Element> combinedAttributes = new ArrayList<>();
        combinedFilter.add(baseQuery.getQueryPattern());

        for (GraphPartitionJoin join : query.getMandatoryJoins()) {
            List<Element> elts = ElementUtils.toElementList(join.getEntityGraphFragment().getElement());
            Element elt = ElementUtils.groupIfNeeded(elts);
            String lfgn = join.getLazyFetchGroupName();
            // Make the element join with the partition variables
            combinedFilter.add(elt);

            rawFetchGroups.put(lfgn, join);
        }

        for (GraphPartitionJoin join : query.getOptionalJoins()) {
            List<Element> elts = ElementUtils.toElementList(join.getEntityGraphFragment().getElement());
            Element elt = ElementUtils.groupIfNeeded(elts);
            String lfgn = join.getLazyFetchGroupName();
            rawOptionalFetchGroups.put(lfgn, join);
        }


        Collection<GraphPartitionJoin> fetchGroups = new ArrayList<>();
        for (Entry<String, Collection<GraphPartitionJoin>> e : rawFetchGroups.asMap().entrySet()) {
            String groupName = e.getKey();
            if (e.getValue().isEmpty()) {
                continue;
            }
            GraphPartitionJoin newGp = merge(groupName, partitionVars, e.getValue(), false);
            fetchGroups.add(newGp);
        }

        Collection<GraphPartitionJoin> optionalFetchGroups = new ArrayList<>();
        for (Entry<String, Collection<GraphPartitionJoin>> e : rawOptionalFetchGroups.asMap().entrySet()) {
            String groupName = e.getKey();
            if (e.getValue().isEmpty()) {
                continue;
            }

            GraphPartitionJoin newGp = merge(groupName, partitionVars, e.getValue(), true);
            optionalFetchGroups.add(newGp);
        }


        EntityQueryImpl result = new EntityQueryImpl();
        result.setBaseQuery(query.getBaseQuery());
        result.getMandatoryJoins().addAll(fetchGroups);
        result.getOptionalJoins().addAll(optionalFetchGroups);

        return result;
    }


    public static GraphPartitionJoin merge(
            String groupName,
            List<Var> partitionVars,
            Collection<? extends GraphPartitionJoin> gps,
            boolean isOptional) {

        Element newElement = ElementUtils.groupIfNeeded(gps.stream()
                .map(GraphPartitionJoin::getEntityGraphFragment)
                .map(EntityGraphFragment::getElement)
                .map(ElementUtils::toElementList)
                .map(list -> isOptional
                        ? Collections.singletonList(new ElementOptional(ElementUtils.groupIfNeeded(list)))
                        : list)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        BasicPattern bgp = new BasicPattern();
        gps.stream()
                .map(gp -> gp.getEntityGraphFragment().getEntityTemplate().getTemplate().getBGP())
                .forEach(bgp::addAll);
        Template newTemplate = new Template(bgp);

        List<Node> newEntityNodes = gps.stream()
            .map(GraphPartitionJoin::getEntityGraphFragment)
            .map(EntityGraphFragment::getEntityTemplate)
            .map(EntityTemplate::getEntityNodes)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());

        Map<Node, ExprList> newBnodeIdMapping = gps.stream()
                .map(GraphPartitionJoin::getEntityGraphFragment)
                .map(EntityGraphFragment::getEntityTemplate)
                .map(EntityTemplate::getBnodeIdMapping)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));


        EntityTemplate newEntityTemplate = new EntityTemplateImpl(
                newEntityNodes, newTemplate, newBnodeIdMapping);

        EntityGraphFragment newFragment = new EntityGraphFragment(partitionVars, newEntityTemplate, newElement);


        List<Var> parentJoinVars = null; // TODO Handle in the future
        List<GraphPartitionJoin> subJoins = null;
        GraphPartitionJoin result = new GraphPartitionJoin(newFragment, parentJoinVars, groupName, subJoins);
        return result;
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
    		Function<? super Query, ? extends QueryExecution> conn,
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
                .execSelectRaw(() -> conn.apply(selectQuery))
                .compose(aggregateConsecutiveItemsWithSameKey(bindingToKey, aggregator));

        return result;
    }


    public static Flowable<GraphPartitionWithEntities> execConstructPartitionedOld(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryImpl queryEx,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

        EntityQueryBasic assembledQuery = assembleEntityAndAttributeParts(queryEx);

        return execConstructPartitionedOld(conn, assembledQuery, graphSupplier, exprListEval);
    }

    public static Flowable<GraphPartitionWithEntities> execConstructPartitionedOld(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryBasic queryEx,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

//      Model entitySortModel = ModelFactory.createDefaultModel();
        EntityQueryProcessed tmp = processEntityQuery(queryEx, false, graphSupplier, exprListEval);

        return execQueryActual(conn, tmp.partitionVars, tmp.trackedTemplateNodes, tmp.selectQuery, tmp.tableToGraph);
    }

    public static class EntityQueryProcessed {
        protected List<Var> partitionVars;
        protected Query selectQuery;
        protected Set<Node> trackedTemplateNodes;
        protected Function<Table, AccObjectGraph> tableToGraph;

        public EntityQueryProcessed(List<Var> partitionVars, Query selectQuery, Set<Node> trackedTemplateNodes,
                Function<Table, AccObjectGraph> tableToGraph) {
            super();
            this.partitionVars = partitionVars;
            this.selectQuery = selectQuery;
            this.trackedTemplateNodes = trackedTemplateNodes;
            this.tableToGraph = tableToGraph;
        }

        public Query getInnerSelect() {
            ElementGroup grp = (ElementGroup)(selectQuery.getQueryPattern());
            ElementSubQuery subQueryElt = (ElementSubQuery)grp.get(0);
            Query result = subQueryElt.getQuery();

            return result;
        }

        public List<Var> getPartitionVars() {
            return partitionVars;
        }

        public Query getSelectQuery() {
            return selectQuery;
        }

        public Set<Node> getTrackedTemplateNodes() {
            return trackedTemplateNodes;
        }

        public Function<Table, AccObjectGraph> getTableToGraph() {
            return tableToGraph;
        }


    }

    public static EntityQueryProcessed processEntityQuery(
            EntityQueryBasic queryEx,
            boolean forceSubSelect) {
        return processEntityQuery(queryEx, forceSubSelect, GraphFactory::createDefaultGraph, EntityQueryRx::defaultEvalToNode);
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
    public static EntityQueryProcessed processEntityQuery(
            EntityQueryBasic queryEx,
            boolean forceSubSelect,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

        EntityBaseQuery baseQuery = queryEx.getBaseQuery();

        EntityTemplate directTemplate = baseQuery.getEntityTemplate();
        EntityTemplate attributeTemplate = queryEx.getAttributeFragment().getEntityTemplate();
        EntityTemplate optionalTemplate = queryEx.getOptionalAttributeFragment().getEntityTemplate();

        // Combine the direct and attribute templates
//        Map<Var, Var> varMap = VarUtils.createJoinVarMap(
//                sourceVars, targetVars, sourceJoinVars, targetJoinVars, varGen);


        EntityTemplate effectiveTemplate = EntityTemplate.merge(
                directTemplate,
                attributeTemplate,
                optionalTemplate);

        Query standardQuery = baseQuery.getStandardQuery();

        // FIXME Check whether this needs subquery wrapping
        Element filterElement = standardQuery.getQueryPattern();


        //Template template = standardQuery.getConstructTemplate();
        Template template = effectiveTemplate.getTemplate();
        Map<Node, ExprList> idMapping = directTemplate.getBnodeIdMapping();


        List<Var> partitionVars = baseQuery.getPartitionVars();
        List<SortCondition> partitionOrderBy = baseQuery.getPartitionOrderBy();


        Set<Var> essentialProjectVars = getEssentialProjectVars(
                template, idMapping);

//        Function<Binding, Node> bindingToRootNodeInst = rootNode == null
//                ? null
//                : createKeyFunction(rootNode, idMapping, exprListEval);

//        List<Var> entityVars = getEntityVars(rootNode, idMapping);

        Set<Node> trackedTemplateNodes = new LinkedHashSet<>(effectiveTemplate.getEntityNodes());

        Set<Var> blacklist = QueryUtils.mentionedVars(standardQuery);
        Generator<Var> varGen = VarGeneratorBlacklist.create("sortKey", blacklist);


        // If direct template is non-empty then extend the attribute element with the selector
        // (in order to expose the seletors variables as attributes)

        // conversely, if the attribute element is non-optional then add it to the selector


        Element attributeElement = queryEx.getAttributeFragment().getElement(); //standardQuery.getQueryPattern();
        Element optionalAttributeElement = queryEx.getOptionalAttributeFragment().getElement();

        boolean needsSubSelect = forceSubSelect
                || !(partitionOrderBy == null || partitionOrderBy.isEmpty())
                || standardQuery.hasLimit()
                || standardQuery.hasOffset();

        // If there is no need for a subselect then just combine filter and attribute

        List<Element> filterElts = ElementUtils.toElementList(filterElement);
        List<Element> attrElts = ElementUtils.toElementList(attributeElement);
        List<Element> optAttrElts = ElementUtils.toElementList(optionalAttributeElement);

        Element effectiveFilter = filterElement;
        Element effectiveAttribute = attributeElement;

        Query selectQuery;
        if (!needsSubSelect) {
            effectiveFilter = ElementUtils.groupIfNeeded(Iterables.concat(
                    filterElts, attrElts, optAttrElts));
            effectiveAttribute = null;

            standardQuery.setQueryPattern(effectiveFilter);

            selectQuery = preprocessQueryForPartitionWithoutSubSelect(
                    standardQuery,
                    partitionVars,
//                    attributeElement,
                    essentialProjectVars,
                    true);
                    //partitionOrderBy,
                    //true);

        } else {
            effectiveFilter = ElementUtils.groupIfNeeded(Iterables.concat(filterElts, attrElts));

            if (!directTemplate.getTemplate().getTriples().isEmpty()) {
                effectiveAttribute = ElementUtils.groupIfNeeded(Iterables.concat(filterElts, attrElts, optAttrElts));
            } else {
                effectiveAttribute = ElementUtils.groupIfNeeded(Iterables.concat(attrElts, optAttrElts));
            }

            standardQuery.setQueryPattern(effectiveFilter);

            selectQuery = preprocessQueryForPartitionWithSubSelect(
                    standardQuery,
                    partitionVars,
                    effectiveAttribute,
                    essentialProjectVars,
                    partitionOrderBy,
                    varGen);
        }

        System.err.println(selectQuery);

//        selectQuery = preprocessQueryForPartition(
//                standardQuery,
//                partitionVars,
//                attributeElement,
//                essentialProjectVars,
//                partitionOrderBy,
//                varGen);

        Function<Table, AccObjectGraph> tableToGraph = createTableToGraphMapper(
                template,
                trackedTemplateNodes,
                idMapping,
                exprListEval,
                graphSupplier);

        return new EntityQueryProcessed(partitionVars, selectQuery, trackedTemplateNodes, tableToGraph);
    }


    public static Flowable<GraphPartitionWithEntities> execQueryActual(
    		Function<? super Query, ? extends QueryExecution> conn,
            List<Var> partitionVars,
            Set<Node> trackedTemplateNodes,
            Query selectQuery, Function<Table,
            AccObjectGraph> tableToGraph) {
        Flowable<GraphPartitionWithEntities> result = execSelectPartitioned(
                conn, selectQuery, partitionVars)
                /*
                // This map operation sorts the entities based on the ORDER BY sort conditions
                // but this is not really useful; it e.g cannot be used to sort a publication's set of authors
                .map(keyAndTable -> {
                    // Sort the bindings in the table by the sort condition on the entity

                    // SELECT ?entityVars { } GROUP BY ?entityVars ORDER BY sort conditions VALUES table
                    Binding key = keyAndTable.getKey();
                    Table table = keyAndTable.getValue();

                    Query entitySort = new Query();
                    entitySort.setQuerySelectType();
                    entitySort.setQueryPattern(new ElementData(
                            table.getVars(),
                            Lists.newArrayList(table.rows())));

                    Generator<Var> entityVarGen = VarGeneratorBlacklist.create("entitySortKey", blacklist);

                    entitySort = preprocessQueryForPartitionWithSubSelect(entitySort, entityVars, essentialProjectVars, entityOrderBy, entityVarGen);

                    System.out.println(entitySort);
                    Table newTable;
                    try (QueryExecution qe = QueryExecutionFactory.create(entitySort, entitySortModel)) {
                        newTable = resultSetToTable(qe.execSelect());
                    }

                    return Maps.immutableEntry(key, newTable);
                })
                */
                .map(keyAndTable -> {
                    Binding partitionKey = keyAndTable.getKey();
                    Table table = keyAndTable.getValue();

                    AccObjectGraph acc = tableToGraph.apply(table);
                    Graph graph = acc.getValue();
                    Set<Node> entities = trackedTemplateNodes.stream().map(rootNode -> acc.getTrackedNodes(rootNode))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());

                    GraphPartitionWithEntities r = new GraphPartitionWithEntities(partitionKey, graph, entities);
                    return r;
                });

        return result;
    }


    /**
     * Execute a CONSTRUCT query w.r.t. partitions. For every partition a graph fragment is constructed
     * based on bindings that fell into the partition.
     * In addition, designate all values in that partition that were bound to the node referred to by
     * {@link EntityQueryBasic#getEntityNode()} as 'roots' of that partition.
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
    public static Flowable<RDFNode> execConstructEntities(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryBasic queryEx,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

        Flowable<RDFNode> result = execConstructPartitionedOld(conn, queryEx, graphSupplier, exprListEval)
            .flatMap(graphPartition -> Flowable.fromIterable(graphPartition.getRoots())
                    .map(node -> {
                        Graph graph = graphPartition.getGraph();
                        Model model = ModelFactory.createModelForGraph(graph);
                        RDFNode r = ModelUtils.convertGraphNodeToRDFNode(node, model);
                        return r;
                    }));

        return result;
    }

    public static Flowable<Quad> execConstructEntitiesNg(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryBasic queryEx) {
        return execConstructEntitiesNg(conn, queryEx, GraphFactory::createDefaultGraph, EntityQueryRx::defaultEvalToNode);
    }


    /**
     * Stream the result of an entity query as named graphs
     *
     *
     * @return
     */
    public static Flowable<Quad> execConstructEntitiesNg(
    		Function<? super Query, ? extends QueryExecution> conn,
            EntityQueryBasic queryEx,
            Supplier<Graph> graphSupplier,
            ExprListEval exprListEval) {

        Random random = new Random();

        String namedGraphHash = BaseEncoding.base64Url().encode(
                Hashing.sha256().hashLong(random.nextLong()).asBytes());

        Node hasEntity = NodeFactory.createURI("http://sparql.org/hasEntity");

        return execConstructPartitionedOld(conn, queryEx, graphSupplier, exprListEval)
            .zipWith(LongStream.iterate(0, i -> i + 1)::iterator, SimpleEntry::new)
                .flatMap(graphPartitionAndIndex -> {
                    long index = graphPartitionAndIndex.getValue();
                    GraphPartitionWithEntities graphPartition = graphPartitionAndIndex.getKey();

                    Node ngIri = NodeFactory.createURI("urn:sparql-partition:" + namedGraphHash + "-" + index);

                    List<Quad> quads = new ArrayList<>();
                    for (Node entityNode : graphPartition.getRoots()) {
                        Quad q = new Quad(ngIri, ngIri, hasEntity, entityNode);

                        quads.add(q);
                        ExtendedIterator<Triple> it = graphPartition.getGraph().find();
                        while (it.hasNext()) {
                            Quad quad = new Quad(ngIri, it.next());
                            quads.add(quad);
                        }
                    }

                    return Flowable.fromIterable(quads);
                });
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
     * Based on the information present in {@link EntityQueryBasic} return a function that
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


    public static List<Var> getEntityVars(
            Node root,
            Map<Node, ExprList> idMapping) {

        List<Var> result;
        if (root.isVariable()) {
            Var rootVar = (Var)root;
            result = Collections.singletonList(rootVar);
        } else if (root.isBlank()) {
            // The root node must be mapped to ids
            // TODO Currently the limitation is that the mapping must be a list of vars rather than arbitrary expressions
            ExprList el = idMapping.get(root);
            Objects.requireNonNull(el, "blank node as the root must be mapped to id-generating expressions");

            Set<Var> vars = new LinkedHashSet<>();
            ExprVars.varsMentioned(vars, el);

            result = new ArrayList<>(vars);
        } else {
            // Case where the root node is a constant;
            // unlikely to be useful but handled for completeness
            result = Collections.emptyList();
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
            .lift(FlowableOperatorSequentialGroupBy.<ITEM, KEY, Accumulator<? super ITEM, VALUE>>create(
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


//    public static List<Var> getExprListVars(ExprList exprs) {
//        Set<Var> result = new LinkedHashSet<Var>();
//        for (Expr exprs : idMapping.values()) {
//            ExprVars.varsMentioned(result, exprs);
//        }
//
//        return new ArrayList<>(result);
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

//
//    public static Query preprocessQueryForPartition(
//            Query baseQuery,
//            List<Var> partitionVars,
//            Element attributeElement,
//            Set<Var> requiredVars,
//            List<SortCondition> partitionOrderBy,
//            Generator<Var> varGenerator) {
//
//        boolean needsSubSelect = !(partitionOrderBy == null || partitionOrderBy.isEmpty())
//                || baseQuery.hasLimit()
//                || baseQuery.hasOffset();
//
//        Query result = needsSubSelect
//                ? preprocessQueryForPartitionWithSubSelect(baseQuery, partitionVars, attributeElement, requiredVars, partitionOrderBy, varGenerator)
//                : preprocessQueryForPartitionWithoutSubSelect(baseQuery, partitionVars, attributeElement, requiredVars, true);
//
//        System.err.println(result);
//        return result;
//    }


    public static Query preprocessQueryForPartitionWithSubSelect(
            Query entityQuery,
            List<Var> partitionVars,
            Element attributeElement,
            Set<Var> requiredVars,
            List<SortCondition> partitionOrderBy,
            Generator<Var> varGenerator) {

        Query result = preprocessQueryForPartitionWithoutSubSelect(
                entityQuery,
                partitionVars,
                //attributeElement,
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

            subSelect.addOrderBy(new SortCondition(expr, sc.getDirection()));
        }


        // Limit / offset have to be placed on the inner query
        boolean hasSlice = result.hasLimit() || result.hasOffset();
        if (hasSlice) {
            boolean useWrapper = false;

            if (useWrapper) {
                Query sliceWrapper = new Query();
                sliceWrapper.setQuerySelectType();
                sliceWrapper.setQueryResultStar(true);
                sliceWrapper.setQueryPattern(new ElementSubQuery(subSelect));
                result.setQueryPattern(new ElementSubQuery(sliceWrapper));
                subSelect = sliceWrapper;
            }

            subSelect.setLimit(result.getLimit());
            subSelect.setOffset(result.getOffset());

            result.setLimit(Query.NOLIMIT);
            result.setOffset(Query.NOLIMIT);

        }




        ElementGroup newPattern = ElementUtils.createElementGroup(new ElementSubQuery(subSelect));
        ElementUtils.copyElements(newPattern, attributeElement);

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
    public static Query preprocessQueryForPartitionWithoutSubSelect(
            Query baseQuery,
            List<Var> partitionVars,
            // Element attributeElement, // attribute element is assumed to be aligned with baseQueryat this point
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

//Set<Node> trackedNodes = acc.getTrackedNodes(rootNode);
//if (bindingToRootNodeInst != null) {
//
//  Iterator<Binding> it = table.rows();
//  while (it.hasNext()) {
//      Binding binding = it.next();
//      Node inst = bindingToRootNodeInst.apply(binding);
//      rootNodes.add(inst);
//  }
//}