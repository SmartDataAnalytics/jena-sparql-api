package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class ConjureBuilderImpl
	implements ConjureBuilder
{
	public static void main(String[] args) {
		
		String url = "http://localhost/~raven/test.hdt";
		
		ConjureBuilder cj = new ConjureBuilderImpl(/* add query parser */);
		Op op = cj.coalesce(
				cj.fromUrl(url).hdtHeader().construct("CONSTRUCT WHERE { ?s <urn:tripleCount> ?o }"),
				cj.fromUrl(url).tripleCount()).getOp();
		

		RDFDataMgr.write(System.out, op.getModel(), RDFFormat.TURTLE_PRETTY);
	}

	
	@Override
	public ConjureFluent fromUrl(String url) {
		return ConjureFluentImpl.wrap(OpDataRefResource.from(DataRefUrl.create(url)));
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
