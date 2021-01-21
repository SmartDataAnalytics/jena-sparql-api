package org.aksw.jena_sparql_api.analytics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.decision_tree.api.ConditionalVarDefinitionImpl;
import org.aksw.jena_sparql_api.decision_tree.api.DecisionTreeSparqlExpr;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDF;

public class SchemaMapperImpl {
	
	// The source column names
	protected Set<Var> sourceVars;
	
	// Statistic suppliers
	protected Function<? super Var, ? extends Set<String>> sourceVarToDatatypes;
	protected Function<? super Var, ? extends Number> sourceVarToNulls;

	
	protected TypePromoter typePromotionStrategy;

	
	
//	Map<Var, Multiset<String>> varToDatatypes, // If we don't need frequences we could just use Multimap<Var, String>
//	Map<String, String> typePromotions // casts such as xsd:int to xsd:decimal

	public SchemaMapperImpl setSourceVars(Set<Var> sourceVars) {
		this.sourceVars = sourceVars;
		return this;
	}

	public SchemaMapperImpl setSourceVarToDatatypes(Function<? super Var, ? extends Set<String>> sourceVarToDatatypes) {
		this.sourceVarToDatatypes = sourceVarToDatatypes;
		return this;
	}

	public SchemaMapperImpl setSourceVarToNulls(Function<? super Var, ? extends Number> sourceVarToNulls) {
		this.sourceVarToNulls = sourceVarToNulls;
		return this;
	}

	public SchemaMapperImpl setTypePromotionStrategy(TypePromoter typePromotionStrategy) {
		this.typePromotionStrategy = typePromotionStrategy;
		return this;
	}

	// TODO Finish implementation
	public Map<String, String> createSchemaMapping() {
		ConditionalVarDefinitionImpl tgtMapping = new ConditionalVarDefinitionImpl();

		Map<Var, NodeMapper<?>> columnToJavaClass = new HashMap<>();
		Set<Var> nullableColumns = new HashSet<>();
		
		for (Var srcVar : sourceVars) {
			String srcVarName = srcVar.getName(); 
			
			
			Set<Var> columns = new LinkedHashSet<>();
			
			Set<String> datatypeIris = sourceVarToDatatypes.apply(srcVar);
			Number nullStats = sourceVarToNulls.apply(srcVar);
			boolean isNullable = nullStats.longValue() > 0;

			Map<String, String> typePromotions = typePromotionStrategy.promoteTypes(datatypeIris);

			
			boolean singleDatatype = datatypeIris.size() == 1;
			for (String datatypeIri : datatypeIris) {

				
				String castDatatypeIri = typePromotions.getOrDefault(datatypeIri, datatypeIri);
				
				String baseName = singleDatatype
						? srcVarName
						: srcVarName + "_" + SplitIRI.localname(datatypeIri); // TODO Resolve name clashes such as rdf:type - custom:type
				
				Var tgtVar = Var.alloc(baseName);
				
				DecisionTreeSparqlExpr dt = new DecisionTreeSparqlExpr();
				
				if (!castDatatypeIri.equals(datatypeIri)) {
					E_Function castExpr = new E_Function(castDatatypeIri, new ExprList(new ExprVar(srcVar)));
					dt.getRoot().getOrCreateLeafNode(null).setValue(castExpr);
					tgtMapping.put(tgtVar, dt);	
					columnToJavaClass.put(srcVar, NodeMappers.fromDatatypeIri(castDatatypeIri));
				} else {
					dt.getRoot().getOrCreateLeafNode(null).setValue(new ExprVar(srcVar));
					tgtMapping.put(tgtVar, dt);	
					columnToJavaClass.put(srcVar, NodeMappers.fromDatatypeIri(datatypeIri));					
				}
				
				// Add an extra language column if langString is used
				if (datatypeIri.equals(RDF.langString.getURI())) {
					Var tgtLangVar = Var.alloc(baseName + "_lang");
					DecisionTreeSparqlExpr langDt = new DecisionTreeSparqlExpr();
					langDt.getRoot().getOrCreateLeafNode(null).setValue(new E_Lang(new ExprVar(srcVar)));
					tgtMapping.put(tgtLangVar, langDt);					
					columnToJavaClass.put(srcVar, NodeMappers.from(String.class));
				}				
			}
		}
		
		
		
		
		return null;
	}
	
	public static String createColumnName(String varName, String datatypeIri) {
		return varName + datatypeIri;
	}
	
}
