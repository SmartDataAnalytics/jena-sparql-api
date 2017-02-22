package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.ExpressionPredicate;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.PredicateBase;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.RootImpl;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.VExpression;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.OrderedMap;

/**
 * Criteria query implementation.
 * 
 * Collects clauses of criteria query (e.g. select projections, from/join, where
 * conditions, order by). Eventually translates these clauses to a similar form
 * of Expression tree that can be interpreted and executed against a data store
 * by OpenJPA kernel.
 * 
 * @author Pinaki Poddar
 * @author Fay Wang
 *
 * @since 2.0.0
 */
class CriteriaQueryImpl<T> implements CriteriaQuery<T> {
	private static final Localizer _loc = Localizer.forPackage(CriteriaQueryImpl.class);

	protected Metamodel model;
	//protected Function<Class<X>, EntityType<X>> entityTypeProvider;
	
	protected Set<Root<?>> roots;
	protected List<Predicate> where = new ArrayList<>();
	protected List<Order> orders;
	protected OrderedMap<Object, Class<?>> params; /*
													 * <ParameterExpression<?>,
													 * Class<?>>
													 */
	protected Selection<? extends T> selection;
	protected List<Selection<?>> selections = new ArrayList<>();
	protected List<Expression<?>> groups;
	protected Predicate having;
	protected List<Subquery<?>> subqueries;
	protected boolean distinct;
	protected Subquery<?> delegator;
	protected Class<T> resultClass;
	protected boolean compiled;

	// AliasContext
	protected int aliasCount = 0;
	protected static String ALIAS_BASE = "autoAlias";

	protected Map<Selection<?>, Value> _variables = new HashMap<Selection<?>, Value>();
	protected Map<Selection<?>, Value> _values = new HashMap<Selection<?>, Value>();
	protected Map<Selection<?>, String> _aliases = null;
	protected Map<Selection<?>, Value> _rootVariables = new HashMap<Selection<?>, Value>();
	
	
	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		return new RootImpl<>(entityClass);
//		EntityType<X> entityType = model.entity(entityClass);
//		Root<X> result = from(entityType);
//		return result;
	}

	@Override
	public <X> Root<X> from(EntityType<X> entityType) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Root<?>> getRoots() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Selection<T> getSelection() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Predicate getRestriction() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<Expression<?>> getGroupList() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Predicate getGroupRestriction() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isDistinct() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Class<T> getResultType() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CriteriaQuery<T> select(Selection<? extends T> selection) {
		selections.add(selection);
		return this;
	}

	@Override
	public CriteriaQuery<T> multiselect(Selection<?>... selections) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
		// TODO Auto-generated method stub
		return null;
	}
		
	
	public static Predicate wrapAsPredicate(Expression<Boolean> expr) {
		Class<?> exprClass = expr.getClass();
		Predicate result = PredicateBase.class.isAssignableFrom(exprClass)
				? (Predicate)expr
				: new ExpressionPredicate((VExpression<Boolean>)expr);
		return result;
	}
	
	@Override
	public CriteriaQuery<T> where(Expression<Boolean> restriction) {
		where.add(wrapAsPredicate(restriction));
		return this;
	}

	@Override
	public CriteriaQuery<T> where(Predicate... restrictions) {
		Arrays.asList(restrictions).stream()
			.map(CriteriaQueryImpl::wrapAsPredicate)
			.forEach(where::add);

		return this;		
	}

	@Override
	public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
		return this;
	}
	@Override
	public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CriteriaQuery<T> having(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CriteriaQuery<T> having(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CriteriaQuery<T> orderBy(Order... o) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CriteriaQuery<T> orderBy(List<Order> o) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public CriteriaQuery<T> distinct(boolean distinct) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<Order> getOrderList() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Set<ParameterExpression<?>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
