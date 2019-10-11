package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;

public class ConjureBuilderImpl
	implements ConjureBuilder
{	
	@Override
	public ConjureFluent fromUrl(String url) {
		return ConjureFluentImpl.wrap(OpDataRefResource.from(DataRefUrl.create(url)));
	}

	@Override
	public ConjureFluent fromVar(String name) {
		return ConjureFluentImpl.wrap(OpVar.create(name));
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
	public ConjureFluent union(ConjureFluent... conjureFluents) {
		return ConjureFluentImpl.wrap(OpUnion.create(toOps(conjureFluents)));
	}


	@Override
	public ConjureFluent coalesce(ConjureFluent... conjureFluents) {
		return ConjureFluentImpl.wrap(OpCoalesce.create(toOps(conjureFluents)));
	}

}
