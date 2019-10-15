package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.conjure.job.api.Macro;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Call of a macro
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpMacroCall
	extends OpN
{
	@IriNs("rpif")
	Macro getMacro();
	OpMacroCall setMacro(Macro macro);
	
	
	@Override
	OpMacroCall setSubOps(List<Op> subOps);

	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpMacroCall clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpMacroCall.class)
				.setSubOps(subOps);
	}

	
	public static OpMacroCall create(Model model, Macro macro, Op ...subOps) {
		return create(model, macro, Arrays.asList(subOps));
	}

	public static OpMacroCall create(Model model, Macro name, List<Op> subOps) {
//		Model model = subOps.size() > 0 ? subOps.get(0).getModel() : ModelFactory.createDefaultModel();
		// Model model = ModelFactory.createDefaultModel();
		model = model != null ? model : ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			model.add(subOp.getModel());
		}
		
		OpMacroCall result = model.createResource().as(OpMacroCall.class)
			.setSubOps(subOps);
		
		return result;
	}
}
