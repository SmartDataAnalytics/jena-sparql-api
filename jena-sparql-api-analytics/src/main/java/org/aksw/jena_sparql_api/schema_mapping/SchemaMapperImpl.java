package org.aksw.jena_sparql_api.schema_mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.analytics.ResultSetAnalytics;
import org.aksw.jena_sparql_api.decision_tree.api.ConditionalVarDefinitionImpl;
import org.aksw.jena_sparql_api.decision_tree.api.DecisionTreeSparqlExpr;
import org.aksw.jena_sparql_api.decision_tree.api.E_SerializableIdentity;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Datatype;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.LoggerFactory;


/**
 * SchemaMapper for mapping RDF tables ("result sets") to SQL tables.
 * The schema mapper is independent of any instance data an operates only based on
 * a given set of source variables (column names) together with statistic providers.
 * 
 * As a consequence it is possible to create schema mappings for concrete SPARQL result sets
 * as well as from schemas of virtual result sets such as obtained by rewriting a SPARQL query
 * w.r.t. a set of R2RML mappings.
 *
 * Example for using this class in conjunction with {@link ResultSetAnalytics} on a concrete SPARQL result set:
 * 
 * <pre>
 * {@code
 * List<Binding> bindings = rs...;
 * Set<Var> resultVars = rs...;
 *
 * Map<Var, Entry<Multiset<String>, Long>> usedDatatypesAndNulls = bindings.stream()
 *    .collect(ResultSetAnalytics.usedDatatypesAndNullCounts(resultVars).asCollector());
 *
 * SchemaMapperImpl.newInstance()
 *     .setSourceVars(resultVars)
 *     .setSourceVarToDatatypes(v -> usedDatatypesAndNulls.get(v).getKey().elementSet())
 *     .setSourceVarToNulls(v -> usedDatatypesAndNulls.get(v).getValue())
 *     .setTypePromotionStrategy(TypePromoterImpl.create())
 *     .createSchemaMapping();
 * }
 * </pre>
 * 
 * @author Claus Stadler
 *
 */
public class SchemaMapperImpl {
	// private static final Logger logger = LoggerFactory.getLogger(SchemaMapperImpl.class);
	
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

	public SchemaMapping createSchemaMapping() {
		
		Map<Var, FieldMapping> tgtVarToMapping = new HashMap<>(); 
		
		for (Var srcVar : sourceVars) {
			String srcVarName = srcVar.getName(); 
			
			// System.out.println("Processing srcVar: " + srcVar);
			ExprVar srcExprVar = new ExprVar(srcVar);
			
			Set<String> datatypeIris = sourceVarToDatatypes.apply(srcVar);
			Number nullStats = sourceVarToNulls.apply(srcVar);

			Map<String, String> typePromotions = typePromotionStrategy.promoteTypes(datatypeIris);

			
			boolean singleDatatype = datatypeIris.size() == 1;
			
			boolean isNullable = nullStats.longValue() > 0 || !singleDatatype;
			
			
			for (String datatypeIri : datatypeIris) {

				//System.out.println("Processing datatypeIri: " + datatypeIri);
				
				String castDatatypeIri = typePromotions.getOrDefault(datatypeIri, datatypeIri);
				
				String baseName = singleDatatype
						? srcVarName
						: srcVarName + "_" + SplitIRI.localname(datatypeIri); // TODO Resolve name clashes such as rdf:type - custom:type
				
				Var tgtVar = Var.alloc(baseName);
				
				DecisionTreeSparqlExpr dt = new DecisionTreeSparqlExpr();
				
				if (!castDatatypeIri.equals(datatypeIri)) {
					dt.getRoot()
						.getOrCreateInnerNode(null, E_SerializableIdentity.wrap(
								createDatatypeCheck(srcExprVar, castDatatypeIri)))				
						.getOrCreateLeafNode(NodeValue.TRUE.asNode())
							.setValue(E_SerializableIdentity.wrap(new E_Function(castDatatypeIri, new ExprList(srcExprVar))));

//					tgtMapping.put(tgtVar, dt);	
					// columnToJavaClass.put(srcVar, NodeMappers.fromDatatypeIri(castDatatypeIri));
//					targetVarType.put(tgtVar, castDatatypeIri);
					tgtVarToMapping.put(tgtVar, new FieldMappingImpl(tgtVar, dt, castDatatypeIri, isNullable));

//					System.out.println(tgtMapping);

				} else {
					dt.getRoot()
					.getOrCreateInnerNode(null, E_SerializableIdentity.wrap(
						createDatatypeCheck(srcExprVar, castDatatypeIri)))					
					.getOrCreateLeafNode(NodeValue.TRUE.asNode())
						.setValue(E_SerializableIdentity.wrap(srcExprVar));
					
//					tgtMapping.put(tgtVar, dt);	
//					columnToJavaClass.put(srcVar, NodeMappers.fromDatatypeIri(datatypeIri));
//					targetVarType.put(tgtVar, datatypeIri);
					tgtVarToMapping.put(tgtVar, new FieldMappingImpl(tgtVar, dt, datatypeIri, isNullable));

					
//					System.out.println(tgtMapping);

				}
				
				// Add an extra language column if langString is used
				if (datatypeIri.equals(RDF.langString.getURI())) {
					Var tgtLangVar = Var.alloc(baseName + "_lang");
					DecisionTreeSparqlExpr langDt = new DecisionTreeSparqlExpr();
					langDt.getRoot()
						.getOrCreateInnerNode(null, E_SerializableIdentity.wrap(createDatatypeCheck(srcExprVar, RDF.Nodes.langString.getURI())))					
						.getOrCreateLeafNode(NodeValue.TRUE.asNode()).setValue(E_SerializableIdentity.wrap(new E_Lang(srcExprVar)));
//					tgtMapping.put(tgtLangVar, langDt);					
					// columnToJavaClass.put(srcVar, NodeMappers.from(String.class));
					tgtVarToMapping.put(tgtLangVar, new FieldMappingImpl(tgtLangVar, langDt, XSD.xstring.getURI(), isNullable));
				}
			}
		}
		
		
		SchemaMappingImpl result = new SchemaMappingImpl(tgtVarToMapping);

		// System.out.println(result);

		
		return result;
	}
	
	public static Expr createDatatypeCheck(Expr expr, String datatypeIri) {
		Expr result;
		switch (datatypeIri) {
		case NodeUtils.R2RML_IRI: result = new E_IsIRI(expr); break;
		case NodeUtils.R2RML_BlankNode: result = new E_IsBlank(expr); break;
		default: result = new E_Equals(
				new E_Datatype(expr),
				NodeValue.makeNode(NodeFactory.createURI(datatypeIri)));
		}
		
		return result;
	}
	
	public static String createColumnName(String varName, String datatypeIri) {
		return varName + datatypeIri;
	}
	
	
	public static SchemaMapperImpl newInstance() {
		return new SchemaMapperImpl();
	}
}
