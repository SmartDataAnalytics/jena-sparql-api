package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSequence;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;

public class ConjureBuilderImpl
	implements ConjureBuilder
{	
	protected ConjureContext context;

	public static ConjureBuilder start() {
		return new ConjureBuilderImpl();
	}

	public ConjureBuilderImpl() {
		this(new ConjureContext());
	}
	
	public ConjureBuilderImpl(ConjureContext context) {
		super();
		this.context = context;
	}

	@Override
	public ConjureContext getContext() {
		return context;
	}

	public ConjureFluent wrap(Op op) {
		return new ConjureFluentImpl(context, op);
	}

	@Override
	public ConjureFluent fromUrl(String url) {
		return wrap(OpDataRefResource.from(context.getModel(), DataRefUrl.create(context.getModel(), url)));
	}

	@Override
	public ConjureFluent fromVar(String name) {
		return wrap(OpVar.create(context.getModel(), name));
	}

	@Override
	public ConjureFluent fromDataRefFn(Function<? super Model, ? extends DataRef> dataRefFn) {
		Model model = context.getModel();
		DataRef dataRef = dataRefFn.apply(model);
		return wrap(OpDataRefResource.from(context.getModel(), dataRef));		
	}
	
	@Override
	public ConjureFluent fromDataRef(DataRef dataRef) {
		// Copy all triples of the dataref into the model of the fluent
		DataRef copy = JenaPluginUtils.copyInto(dataRef, DataRef.class, context.getModel());
		return wrap(OpDataRefResource.from(context.getModel(), copy));
	}
	
	public static List<Op> toOps(ConjureFluent... conjureFluents) {
		return toOps(Arrays.asList(conjureFluents));
	}

	public static List<Op> toOps(List<? extends ConjureFluent> conjureFluents) {
		List<Op> result = conjureFluents.stream()
				.map(ConjureFluent::getOp)
				.collect(Collectors.toList());
		return result;
	}

	@Override
	public ConjureFluent seq(ConjureFluent... conjureFluents) {
		return wrap(OpSequence.create(context.getModel(), toOps(conjureFluents)));
	}

	@Override
	public ConjureFluent union(ConjureFluent... conjureFluents) {
		return wrap(OpUnion.create(context.getModel(), toOps(conjureFluents)));
	}

	@Override
	public ConjureFluent coalesce(ConjureFluent... conjureFluents) {
		return wrap(OpCoalesce.create(context.getModel(), toOps(conjureFluents)));
	}

	@Override
	public ConjureFluent call(String macroName, ConjureFluent... conjureFluents) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public ConjureFluent fromEmptyModel() {
		return wrap(OpData.create(context.getModel()));
	}
}
