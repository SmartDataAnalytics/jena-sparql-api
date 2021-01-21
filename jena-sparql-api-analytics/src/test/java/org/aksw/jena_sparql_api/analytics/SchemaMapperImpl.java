package org.aksw.jena_sparql_api.analytics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.decision_tree.api.ConditionalVarDefinitionImpl;
import org.aksw.jena_sparql_api.decision_tree.api.DecisionTreeSparqlExpr;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Datatype;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.Multiset;

public class SchemaMapperImpl {
	

	// TODO Finish implementation
	public Map<String, String> createSchema(
			Map<Var, Multiset<String>> varToDatatypes, // If we don't need frequences we could just use Multimap<Var, String>
			Map<String, String> typePromotions // casts such as xsd:int to xsd:decimal
		) {
		ConditionalVarDefinitionImpl tgtMapping = new ConditionalVarDefinitionImpl();

		Map<Var, NodeMapper<?>> columnToJavaClass = new HashMap<>();
		Set<Var> nullableColumns = new HashSet<>();
		
		for (Entry<Var, Multiset<String>> e : varToDatatypes.entrySet()) {
			Var srcVar = e.getKey();
			String srcVarName = srcVar.getName(); 
			
			
			// Map<Var, Var>
			Set<Var> columns = new LinkedHashSet<>();
			
			Set<String> datatypeIris = e.getValue().elementSet();

			boolean singleDatatype = datatypeIris.size() == 1;
			for (String datatypeIri : e.getValue().elementSet()) {

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
