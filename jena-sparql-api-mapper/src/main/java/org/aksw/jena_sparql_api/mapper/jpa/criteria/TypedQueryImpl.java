package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptOps;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.OrderedConcept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngine;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.ExpressionCompiler;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.VExpression;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.VPath;
import org.aksw.jena_sparql_api.mapper.model.TypeDecider;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



class AliasMapper
    implements Function<Expression<?>, String>
{
    //protected Set<String> aliasBlacklist = new HashSet<>();
    //protected Generator<String> aliasGenerator;
    protected Map<Expression<?>, String> expressionToAlias = new IdentityHashMap<>();
    protected Supplier<String> aliasSupplier;

    public AliasMapper() {
        int[] i = {0};
        aliasSupplier = () -> { return "_a" + ++i[0]; };
    }



    @Override
    public String apply(Expression<?> e) {
        String result = expressionToAlias.computeIfAbsent(e, (x) -> x.getAlias() != null ? e.getAlias() : aliasSupplier.get());

        return result;
    }
}


public class TypedQueryImpl<X>
    implements TypedQuery<X>
{

    private static final Logger logger = LoggerFactory.getLogger(TypedQueryImpl.class);

    protected Integer startPosition = null;
    protected Integer maxResult = null;

    protected CriteriaQuery<X> criteriaQuery;
    protected RdfMapperEngine engine;

    //protected SparqlService sparqlService;
    //protected Concept concept;


    //protected Function<Class<?>, PathResolver> pathResolverFactory;
    Function<Expression<?>, String> aliasMapper = new AliasMapper();
    protected Supplier<ExpressionCompiler> expressionCompilerFactory;


//    protected Query compileQuery() {
//    	Query result = new Query();
//
//    	return result;
//    }

    //Var rootVar = Var.alloc("root");

    /**
     * Each expression compiler yields its own set of elements, but use a common
     * aliasMapper
     *
     * @return
     */
    Supplier<ExpressionCompiler> createExpressionCompilerFactory() {

        //Function<Expression<?>, String> aliasMapper = new AliasMapper();

        return () -> {
            Set<Element> elements = new LinkedHashSet<>();
            return new ExpressionCompiler(elements, path -> resolvePath(path, elements, aliasMapper));
        };
    }

    protected OrderedConcept compileConcept() {
        // In the following, we will assign to every expression object not having an alias set, a fresh unique one.
        // Two non-identical objects are treated as different expressions, even if their states are equal

        //Set<Var> blacklist = new HashSet<Var>();
        //Map<Expression<?>, Var> aliasMap = new IdentityHashMap<>();


        // Get the SPARQL concept from the type decider for the requested entity type
        //engine.getT

        Class<X> resultType = criteriaQuery.getResultType();

        TypeDecider typeDecider = engine.getTypeDecider();
        ResourceShapeBuilder rsb = new ResourceShapeBuilder();

        // TODO Fix the lookup on the typedecider
        //typeDecider.exposeShape(rsb, resultType);
        //ResourceShape.createMappedConcept2(resourceShape, filter, includeGraph)

        //RdfType engine.getRdfTypeFactory().



        //PathResolver pathResolver = engine.createResolver(resultType);//mapperEngine.createResolver(Person.class);

        ExpressionCompiler filterCompiler = expressionCompilerFactory.get(); //new ExpressionCompiler(path -> resolvePath(path, aliasMap));//this::resolvePath);
        //elements.add(new ElementFilter(result));


        Set<Root<?>> roots = criteriaQuery.getRoots();
        Var firstRoot = null; // HACK, add support for proper selections
        for(Root<?> root : roots) {
            Class<?> javaType = root.getJavaType();

            Resource r = ModelFactory.createDefaultModel().createResource();

            engine.getTypeDecider().writeTypeTriples(r, javaType);

            BasicPattern bp = new BasicPattern();
            r.getModel().getGraph().find(Node.ANY, Node.ANY, Node.ANY).toSet().forEach(bp::add);

            String rootName = aliasMapper.apply(root);
            Var rootVar = Var.alloc(rootName);
            firstRoot = firstRoot == null ? rootVar : firstRoot;

            bp = NodeTransformLib.transform(new NodeTransformRenameMap(Collections.singletonMap(r.asNode(), rootVar)), bp);

            ElementTriplesBlock etb = new ElementTriplesBlock(bp);

            filterCompiler.getElements().add(etb);

            //lengine.getTypeDecider().writeTypeTriples(outResource, entity);
            //RdfType rdfType = engine.getRdfTypeFactory().forJavaType(javaType);
            //rdfType
            //engine.getTypeDecider().writeTypeTriples(outResource, entity);
        }


        VExpression<?> filterEx = (VExpression<?>)criteriaQuery.getRestriction();
        Concept filterConcept;
        if(filterEx != null) {
            Expr expr = filterEx.accept(filterCompiler);
            filterCompiler.getElements().add(new ElementFilter(expr));
        }

        if(!filterCompiler.getElements().isEmpty()) {

            Element filterEl = ElementUtils.groupIfNeeded(filterCompiler.getElements());

            filterConcept = new Concept(filterEl, firstRoot);
        } else {
            filterConcept = ConceptUtils.createSubjectConcept();
        }

        ExpressionCompiler orderCompiler = expressionCompilerFactory.get(); //new ExpressionCompiler(this::resolvePath);


        // Compile selection - TODO This is a hack right now
//        VExpression<?> selectionEx = (VExpression<?>)criteriaQuery.getSelection();
//        if(selectionEx != null) {
//            selectionEx.accept(filterCompiler);
//        }

//        for(Selection<?> selection : selections) {
//
//        }


        // Compile order
        List<SortCondition> sortConditions = new ArrayList<>();
        for(Order order : criteriaQuery.getOrderList()) {
            VExpression<?> x = ((OrderImpl)order).getExpression();
            Expr e = x.accept(orderCompiler);

            int dir = order.isAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING;
            sortConditions.add(new SortCondition(e, dir));
        }
        Element orderEl = ElementUtils.groupIfNeeded(orderCompiler.getElements());
        Concept orderC = new Concept(orderEl, firstRoot);

        OrderedConcept orderConcept = new OrderedConcept(orderC, sortConditions);
        logger.debug("Sort conditions: " + sortConditions);

        OrderedConcept result = ConceptOps.applyOrder(filterConcept, orderConcept, null);

        // TODO Merge in the rdftype's concept



        // TODO Handle the root variable in a better way

        return result;
    }

    public Var resolvePath(VPath<?> path, Set<Element> elements, Function<Expression<?>, String> aliasMapper) {
        // Get the root type of the path
        Class<?> rootClass = PathResolverVarMapper.getRootClass(path);
        PathResolver rootResolver = engine.createResolver(rootClass);

        PathResolverVarMapper pathMapper = new PathResolverVarMapper(rootResolver, elements, aliasMapper);

        Var result = path.accept(pathMapper);

        return result;
    }

    /*
    public void compileOrder() {

    }*/


    public TypedQueryImpl(CriteriaQuery<X> criteriaQuery, RdfMapperEngine engine) {//SparqlService sparqlService, Concept concept) {
        this.criteriaQuery = criteriaQuery;
        this.engine = engine;

        expressionCompilerFactory = this.createExpressionCompilerFactory();
    }


    @Override
    public X getSingleResult() {
        List<X> items = getResultList(2);
        if(items.size() > 1) {
            throw new NonUniqueResultException();
        }

        X result = Iterables.getFirst(items, null);
        return result;
    }

    @Override
    public TypedQuery<X> setMaxResults(int maxResult) {
        this.maxResult = maxResult;
        return this;
    }

    @Override
    public TypedQuery<X> setFirstResult(int startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    @Override
    public int getMaxResults() {
        return maxResult;
    }

    @Override
    public int getFirstResult() {
        return startPosition;
    }

    @Override
    public int executeUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getHints() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter<?> getParameter(int position) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParameterValue(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParameterValue(int position) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public FlushModeType getFlushMode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public LockModeType getLockMode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public List<X> getResultList() {
        List<X> result = getResultList(null);
        return result;
    }

    // TODO The limit argument can be removed, as this is is read from the maxResult field
    public List<X> getResultList(Integer limit) { //, Integer offset) {
        Long l = maxResult != null ? maxResult.longValue() : null;
        //l = limit != null ? limit.longValue() : null;
        Long o = startPosition != null ? startPosition.longValue() : null;

        SparqlService sparqlService = engine.getSparqlService();


        OrderedConcept orderedConcept = compileConcept();
        Var resultVar = orderedConcept.getConcept().getVar();

        Query query = ConceptUtils.createQueryList(orderedConcept, l, o);


        Selection<X> selection = criteriaQuery.getSelection();

        // TODO The check against Root is somewhat hacky, as we may need to reuse its alias
        if(selection != null && !(selection instanceof Root)) {
            ExpressionCompiler selectionCompiler = expressionCompilerFactory.get(); //new ExpressionCompiler(this::resolvePath);

            VExpression<X> e = (VExpression<X>)selection;
            Expr expr = e.accept(selectionCompiler);

            //Concept tmp = new Concept(selectionCompiler.getElements(), rootVar);

            List<Element> els = new ArrayList<>();
            if(!orderedConcept.getConcept().isSubjectConcept()) {
                els.add(query.getQueryPattern());
            }


            els.addAll(selectionCompiler.getElements());
            Element x = ElementUtils.groupIfNeeded(els);

            query.setQueryPattern(x);


            // Append the elements to the query
            if(expr instanceof ExprAggregator) {
                Aggregator agg = ((ExprAggregator) expr).getAggregator();
                expr = query.allocAggregate(agg);
            }

            query.getProject().clear();
            query.getProject().add(resultVar, expr);


            //selection.filterCompiler
        }

        logger.debug("Query: " + query);

        //compileOrder();

        //List<Node> items = ServiceUtils.fetchList(sparqlService.getQueryExecutionFactory(), concept, 1l, startPosition == null ? null : startPosition.longValue());
        //List<Node> items = ServiceUtils.fetchList(sparqlService.getQueryExecutionFactory(), concept, l, o);
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        List<Node> items = ServiceUtils.fetchList(qef, query, orderedConcept.getConcept().getVar());


        List<X> result = items.stream().map(node -> {
            Class<X> clazz = criteriaQuery.getResultType();
            Object r = engine.find(clazz, node);
            return (X)r;
        }).collect(Collectors.toList());
        logger.debug("GOT " + items + " for concept" + query);

//    	X result;
//    	if(items.isEmpty()) {
//    		result = null;
//    	} else {
//    		Node node = items.iterator().next();
//        	Class<X> clazz = criteriaQuery.getResultType();
//        	result = engine.find(clazz, node);
//    	}

        return result;
    }

    @Override
    public TypedQuery<X> setHint(String hintName, Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value,
            TemporalType temporalType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value,
            TemporalType temporalType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(String name, Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(String name, Calendar value,
            TemporalType temporalType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(String name, Date value,
            TemporalType temporalType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(int position, Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(int position, Calendar value,
            TemporalType temporalType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setParameter(int position, Date value,
            TemporalType temporalType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lockMode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
