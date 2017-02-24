package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngine;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.ExpressionCompiler;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.PathImpl;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.VExpression;
import org.aksw.jena_sparql_api.mapper.test.Person;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;


class PathResolverUtil {
	protected Set<Var> blacklist = new HashSet<>();
	protected Generator<Var> varGen = VarGeneratorBlacklist.create(blacklist);
	
	public Relation resolvePath(PathResolver pathResolver, Path<?> path) {
		blacklist.addAll(VarUtils.toSet(pathResolver.getAliases()));
		
		
		List<String> list = new ArrayList<>();
		
		Path<?> current = path;
		while(current != null) {
			PathImpl<?> p = (PathImpl<?>)current;
			list.add(p.getAttributeName());
			current = p.getParentPath();
		}

		Collections.reverse(list);
		
		PathResolver x = pathResolver;
		for(String attr : list) {
			if(x != null && attr != null) {
				x = x.resolve(attr);
			}
		}
		
		Relation result = x == null ? null : x.getOverallRelation(varGen);
		System.out.println("Resolved path: " + result);
		return result;
	}
	
}


public class TypedQueryImpl<X>
    implements TypedQuery<X>
{
	protected Integer startPosition = null;
	protected Integer maxResult = null;
	
	protected CriteriaQuery<X> criteriaQuery;
	protected RdfMapperEngine engine;

    //protected SparqlService sparqlService;
    //protected Concept concept;

    
    protected Function<Class<?>, PathResolver> pathResolverFactory;

    
//    protected Query compileQuery() {
//    	Query result = new Query();
//    	
//    	return result;
//    }

    protected Concept compileConcept() {
        
    	//RdfType engine.getRdfTypeFactory().
    	
    	
        PathResolver pathResolver = engine.createResolver(Person.class);//mapperEngine.createResolver(Person.class);

        PathResolverUtil pathResolverUtil = new PathResolverUtil();
        
        ExpressionCompiler compiler = new ExpressionCompiler(
        	path -> pathResolverUtil.resolvePath(pathResolver, path)
        );        

    	((VExpression<?>)criteriaQuery.getRestriction()).accept(compiler);
    	System.out.println(compiler.getElements());

    	
    	

    	
    	// Compile order
    	List<SortCondition> sortConditions = new ArrayList<>();
    	for(Order order : criteriaQuery.getOrderList()) {
    		VExpression<?> x = ((OrderImpl)order).getExpression();
    		Expr e = x.accept(compiler);
    		
    		int dir = order.isAscending() ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING;
    		sortConditions.add(new SortCondition(e, dir));    		
    	}
    	System.out.println("Sort conditions: " + sortConditions);
    	
    	Element el = ElementUtils.groupIfNeeded(compiler.getElements());
    	
    	// TODO Merge in the rdftype's concept
    	// TODO Handle the root variable in a better way
    	
    	Concept result = new Concept(el, Var.alloc("root"));
    	return result;    	
    }

    /*
    public void compileOrder() {
        
    }*/

    
    public TypedQueryImpl(CriteriaQuery<X> criteriaQuery, RdfMapperEngine engine) {//SparqlService sparqlService, Concept concept) {
    	this.criteriaQuery = criteriaQuery;
    	this.engine = engine;
    }

    
    @Override
    public X getSingleResult() {
    	SparqlService sparqlService = engine.getSparqlService();
    	
    	
    	Concept concept = compileConcept();
    	//compileOrder();
    	
    	List<Node> items = ServiceUtils.fetchList(sparqlService.getQueryExecutionFactory(), concept, 1l, startPosition == null ? null : startPosition.longValue());

    	System.out.println("GOT " + items + " for concept" + concept);
    	
    	X result;
    	if(items.isEmpty()) {
    		result = null;
    	} else {
    		Node node = items.iterator().next();
        	Class<X> clazz = criteriaQuery.getResultType();
        	result = engine.find(clazz, node);    		
    	}
    
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
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
