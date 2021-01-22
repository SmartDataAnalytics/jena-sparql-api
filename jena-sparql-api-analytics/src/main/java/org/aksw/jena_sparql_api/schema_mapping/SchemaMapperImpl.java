package org.aksw.jena_sparql_api.schema_mapping;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.analytics.ResultSetAnalytics;
import org.aksw.jena_sparql_api.decision_tree.api.DecisionTreeSparqlExpr;
import org.aksw.jena_sparql_api.decision_tree.api.E_SerializableIdentity;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimaps;
import org.apache.jena.ext.com.google.common.collect.SetMultimap;
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
		
		Map<Var, FieldMapping> tgtVarToMapping = new LinkedHashMap<>(); 
		
		for (Var srcVar : sourceVars) {
			String srcVarName = srcVar.getName(); 
			
			// System.out.println("Processing srcVar: " + srcVar);
			ExprVar srcExprVar = new ExprVar(srcVar);
			
			Set<String> rawDatatypeIris = sourceVarToDatatypes.apply(srcVar);
//			List<String> datatypeIris = new ArrayList<>(rawDatatypeIris);
//			Collections.sort(datatypeIris);
			
			Number nullStats = sourceVarToNulls.apply(srcVar);

			Map<String, String> typePromotions = typePromotionStrategy.promoteTypes(rawDatatypeIris);

			// Enrich type promotions with reflexive mappings
			new HashSet<>(typePromotions.values()).forEach(x -> typePromotions.put(x, x));
			
			// Add all target types to the rawDatatypeIris
			// rawDatatypeIris.addAll(typePromotions.values());
			
			// Furthermore, if a type has no promotion mapping add it also as a reflexive mapping
			rawDatatypeIris.stream().filter(dt -> !typePromotions.containsKey(dt))
				.forEach(x -> typePromotions.put(x, x));
			
			SetMultimap<String, String> inverse = Multimaps.invertFrom(Multimaps.forMap(typePromotions), 
				    HashMultimap.<String, String>create());
			
			List<String> promotedDatatypeIris = rawDatatypeIris.stream()
					.map(iri -> typePromotions.getOrDefault(iri, iri))
					.sorted()
					.collect(Collectors.toList());
			
			boolean singleDatatype = promotedDatatypeIris.size() == 1;

			boolean isNullable = nullStats.longValue() > 0 || !singleDatatype;
			
			
			for (String datatypeIri : promotedDatatypeIris) {

				//System.out.println("Processing datatypeIri: " + datatypeIri);
				
				// String castDatatypeIri = typePromotions.getOrDefault(datatypeIri, datatypeIri);
				
				String baseName = singleDatatype
						? srcVarName
						: srcVarName + "_" + SplitIRI.localname(datatypeIri).toLowerCase(); // TODO Resolve name clashes such as rdf:type - custom:type
				
				Var tgtVar = Var.alloc(baseName);
				
				// For each source datatype create the mapping to the promoted type
				DecisionTreeSparqlExpr dt = new DecisionTreeSparqlExpr();
				for (String srcDtIri : inverse.get(datatypeIri)) {

					if (!srcDtIri.equals(datatypeIri)) {
						dt.getRoot()
							.getOrCreateInnerNode(null, E_SerializableIdentity.wrap(
									createDatatypeCheck(srcExprVar, srcDtIri)))				
							.getOrCreateLeafNode(NodeValue.TRUE.asNode())
								.setValue(E_SerializableIdentity.wrap(new E_Function(datatypeIri, new ExprList(srcExprVar))));
	
					} else {
						dt.getRoot()
						.getOrCreateInnerNode(null, E_SerializableIdentity.wrap(
							createDatatypeCheck(srcExprVar, datatypeIri)))					
						.getOrCreateLeafNode(NodeValue.TRUE.asNode())
							.setValue(E_SerializableIdentity.wrap(srcExprVar));
					}
				}					
			
				tgtVarToMapping.put(tgtVar, new FieldMappingImpl(tgtVar, dt, datatypeIri, isNullable));

				
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
