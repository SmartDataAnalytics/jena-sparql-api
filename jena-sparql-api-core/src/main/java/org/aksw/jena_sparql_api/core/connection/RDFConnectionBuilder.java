package org.aksw.jena_sparql_api.core.connection;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

public class RDFConnectionBuilder<T, P> {
	protected P parent;
	protected T connection;
	
	public static <T extends RDFConnection> RDFConnectionBuilder<T, ?> start() {
		RDFConnectionBuilder<T, ?> result = new RDFConnectionBuilder<T, Void>(null);
		return result;		
	}

	public static <T extends RDFConnection> RDFConnectionBuilder<T, ?> from(T conn) {
		RDFConnectionBuilder<T, ?> result = new RDFConnectionBuilder<T, Void>(null);
		result.setSource(conn);
		return result;		
	}
	
	public RDFConnectionBuilder(P parent) {
		super();
		this.parent = parent;
	}

	public RDFConnectionBuilder<T, P> defaultModel() {
		setSource(ModelFactory.createDefaultModel());
		return this;
	}
	
	public RDFConnectionBuilder<T, P> setSource(Model model) {
		setSource(DatasetFactory.wrap(model));
		return this;
	}

	@SuppressWarnings("unchecked")
	public RDFConnectionBuilder<T, P> setSource(Dataset dataset) {
		connection = (T)RDFConnectionFactory.connect(dataset);
		
		return this;
	}
	
	public RDFConnectionBuilder<T, P> setSource(T connection) {
		this.connection = connection;
		return this;
	}

	public RDFConnectionBuilder<RDFConnection, P> addQueryTransform(Function<? super Query, ? extends Query> queryTransform) {
		RDFConnection r = RDFConnectionFactoryEx.wrapWithQueryTransform((RDFConnection)this.connection, queryTransform);
		
		return new RDFConnectionBuilder(null).setSource(r);
	}

	public T getConnection() {
		return connection;
	}
	
	public P end() {
		return parent;
	}
}