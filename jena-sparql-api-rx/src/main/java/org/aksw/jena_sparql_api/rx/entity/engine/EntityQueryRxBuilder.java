package org.aksw.jena_sparql_api.rx.entity.engine;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryBasic;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.ExprListEval;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.graph.GraphFactory;

import io.reactivex.rxjava3.core.Flowable;

public class EntityQueryRxBuilder {
	
	protected Function<? super Query, ? extends QueryExecution> queryExecutionFactory = null;
	protected EntityQueryBasic basicEntityQuery = null;
	protected Supplier<Graph> graphSupplier = null;
	protected ExprListEval exprListEval = null;
	
	public static EntityQueryRxBuilder create() {
		return new EntityQueryRxBuilder();
	}
	
    public EntityQueryRxBuilder setQueryExecutionFactory(SparqlQueryConnection conn) {
		this.queryExecutionFactory = conn::query;
		return this;
	}
	
    public EntityQueryRxBuilder setQueryExecutionFactory(Function<? super Query, ? extends QueryExecution> queryExecutionFactory) {
		this.queryExecutionFactory = queryExecutionFactory;
		return this;
	}

	public EntityQueryRxBuilder setQuery(EntityQueryImpl query) {
		this.basicEntityQuery = EntityQueryRx.assembleEntityAndAttributeParts(query);
		return this;
	}

	public EntityQueryRxBuilder Query(EntityQueryBasic query) {
		this.basicEntityQuery = query;
		return this;
	}

	public EntityQueryRxBuilder setGraphSupplier(Supplier<Graph> graphSupplier) {
		this.graphSupplier = graphSupplier;
		return this;
	}

	public EntityQueryRxBuilder setExprListEval(ExprListEval exprListEval) {
		this.exprListEval = exprListEval;
		return this;
	}


	public Flowable<RDFNode> build() {
		Objects.requireNonNull(queryExecutionFactory, "Query execution factory not set");
		Objects.requireNonNull(basicEntityQuery, "Entity query not set");
		
        return EntityQueryRx.execConstructEntities(
        		queryExecutionFactory,
        		basicEntityQuery,
        		Optional.ofNullable(graphSupplier).orElse(GraphFactory::createDefaultGraph),
        		Optional.ofNullable(exprListEval).orElse(EntityQueryRx::defaultEvalToNode));
    }

}
