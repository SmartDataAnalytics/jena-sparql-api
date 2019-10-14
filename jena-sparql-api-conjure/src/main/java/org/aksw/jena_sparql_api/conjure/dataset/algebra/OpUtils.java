package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

public class OpUtils {


	public static Op copyWithSubstitution(Op op, Map<String, ? extends Op> varNameToSubst) {
		Op result = copyWithSubstitution(op, varNameToSubst::get);
		return result;
	}
	
	public static Op copyWithSubstitution(Op op, Function<String, ? extends Op> varNameToSubst) {
		RDFNode resourceCopy = op.inModel
				(ModelFactory.createDefaultModel().add(op.getModel()));
		
		Op workflowCopy = JenaPluginUtils.polymorphicCast(resourceCopy, Op.class);
		
		Op result = substituteVars(workflowCopy, varNameToSubst);
		return result;
	}


	public static Set<String> mentionedVarNames(Op op) {
		Set<OpVar> opVars = mentionedVars(op);
		Set<String> result = opVars.stream()
				.map(OpVar::getName)
				.collect(Collectors.toSet());
		return result;
	}
	
	public static Set<OpVar> mentionedVars(Op op) {
		Set<OpVar> result = Streams.stream(Traverser.forTree(Op::getChildren).depthFirstPostOrder(op))
				.filter(x -> x instanceof OpVar)
				.map(x -> x.as(OpVar.class))
				.collect(Collectors.toSet());
		
		return result;
	}
	
	public static Op substituteVars(Op op, Function<String, ? extends Op> varNameToSubst) {
		
		Set<OpVar> vars = mentionedVars(op);
		
		Op result = op;
		for(OpVar var : vars) {
			String name = var.getName();
			Op replacement = varNameToSubst.apply(name);
			
			if(replacement != null) {
				Op x = OpUtils.<Op>substitute(var, replacement);
				if(Objects.equals(op, x)) {
					result = x;
				}
			}
		}
		
		return result;
	}
	
	public static <T extends RDFNode> T substitute(T tgtNode, T replacement) {
		// Copy statements in case tgtNode shares the backend with replacement
		Resource replRes = replacement.asResource();
		List<Statement> stmts = replRes.listProperties().toList();

		// HACK - We just assume its resources and copy the outgoing properties...
		Resource tgtRes = tgtNode.asResource();
		tgtRes.removeProperties();
		
		tgtRes.getModel().add(replRes.getModel());
		
		replRes.inModel(tgtRes.getModel()).removeProperties();

		for(Statement stmt : stmts) {
			tgtRes.addProperty(stmt.getPredicate(), stmt.getObject());
		}
		
		
		return tgtNode;
	}
}
