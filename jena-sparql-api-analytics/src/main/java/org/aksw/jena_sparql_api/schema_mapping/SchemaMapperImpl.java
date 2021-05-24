package org.aksw.jena_sparql_api.schema_mapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.analytics.ResultSetAnalytics;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;



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
	
	// Function for remapping a type to a different one, such as a xsd:date to a string 
	protected Function<String, String> typeRemap;
	
	// Supplier for converter functions
	protected BiFunction<String, String, ? extends ExprRewrite> typeConversionSupplier;
	
	// If a variable is unbound on every result row then there is no
	// known datatype that can be mapped to
	// If the fallback for a variabl is null then the column is omitted
	protected Function<? super Var, String> varToFallbackDatatype;
	
	protected TypePromoter typePromotionStrategy;

	
	
//	Map<Var, Multiset<String>> varToDatatypes, // If we don't need frequences we could just use Multimap<Var, String>
//	Map<String, String> typePromotions // casts such as xsd:int to xsd:decimal


	/**
	 * Set the set of columns of the source schema (represented as variables) which participate in the schema mapping.
	 */
	public SchemaMapperImpl setSourceVars(Set<Var> sourceVars) {
		this.sourceVars = sourceVars;
		return this;
	}

	public Set<Var> getSourceVars() {
		return sourceVars;
	}

	public SchemaMapperImpl setSourceVarToDatatypes(Function<? super Var, ? extends Set<String>> sourceVarToDatatypes) {
		this.sourceVarToDatatypes = sourceVarToDatatypes;
		return this;
	}

	public SchemaMapperImpl setSourceVarToNulls(Function<? super Var, ? extends Number> sourceVarToNulls) {
		this.sourceVarToNulls = sourceVarToNulls;
		return this;
	}

	public SchemaMapperImpl setVarToFallbackDatatype(Function<? super Var, String> varToFallbackDatatype) {
		this.varToFallbackDatatype = varToFallbackDatatype;
		return this;
	}
	
	/**
	 *  Sets (and overrides) the fallback for any variable to the given argument.
	  * Calls {@link #setVarToFallbackDatatype(Function)}.
	  */ 
	public SchemaMapperImpl setVarToFallbackDatatype(String datatype) {
		return setVarToFallbackDatatype(v -> datatype);
	}

	/**
	 *  Convenience method to set (and override) the fallback datatype to xsd:string.
	 *  Calls {@link #setVarToFallbackDatatype(Function)}.
	 */
	public SchemaMapperImpl setVarToFallbackDatatypeToString() {
		return setVarToFallbackDatatype("http://www.w3.org/2001/XMLSchema#string");
	}
	
	

	public Function<String, String> getTypeRemap() {
		return typeRemap;
	}

	public SchemaMapperImpl setTypeRemap(Function<String, String> typeRemap) {
		this.typeRemap = typeRemap;
		return this;
	}
	

	public BiFunction<String, String, ? extends ExprRewrite> getTypeConversionSupplier() {
		return typeConversionSupplier;
	}

	public SchemaMapperImpl setTypeConversionSupplier(BiFunction<String, String, ? extends ExprRewrite> typeConversionSupplier) {
		this.typeConversionSupplier = typeConversionSupplier;
		return this;
	}

	/**
	 * The type promotion strategy. This can be used to reduce the number of columns in the target
	 * schema by combining weaker types with mightier ones, such as by promoting short to int.
	 * The strategy may even promote integer types to floating point ones.
	 * 
	 * @param typePromotionStrategy
	 * @return
	 */
	public SchemaMapperImpl setTypePromotionStrategy(TypePromoter typePromotionStrategy) {
		this.typePromotionStrategy = typePromotionStrategy;
		return this;
	}

	public static String deriveSuffix(String datatype) {
		String result = SplitIRI.localname(datatype).toLowerCase();
		return result;
	}
	
	public SchemaMapping createSchemaMapping() {
		
		Objects.requireNonNull(sourceVars, "Source Variables not set");
		Objects.requireNonNull(sourceVarToDatatypes, "Mapping of source variables to datatypes not set");

		
		// Obtain effective components / Apply defaults
		BiFunction<String, String, ? extends ExprRewrite> effectiveTypeConversionSupplier = typeConversionSupplier == null
				? SchemaMapperImpl::defaultTypeConversionSupplier
				: typeConversionSupplier;

		TypePromoter effectiveTypePromoter = typePromotionStrategy == null
				? types -> types.stream().collect(Collectors.toMap(e -> e, e -> e))
				: typePromotionStrategy;

		Function<? super Var, ? extends Number> effectiveSourceVarToNulls = sourceVarToNulls == null
				? var -> null
				: sourceVarToNulls;

		Function<? super Var, String> effectiveVarToFallbackDatatype = varToFallbackDatatype == null
				? var -> null
				: varToFallbackDatatype;

		
		
		Map<Var, FieldMapping> tgtVarToMapping = new LinkedHashMap<>(); 
		
		for (Var srcVar : sourceVars) {
			String srcVarName = srcVar.getName(); 
			
			// System.out.println("Processing srcVar: " + srcVar);
			ExprVar srcExprVar = new ExprVar(srcVar);
			
			Set<String> rawDatatypes = sourceVarToDatatypes.apply(srcVar);
//			List<String> datatypeIris = new ArrayList<>(rawDatatypeIris);
//			Collections.sort(datatypeIris);
			
			Number nullStats = effectiveSourceVarToNulls.apply(srcVar);

			Map<String, String> typePromotions = effectiveTypePromoter.promoteTypes(rawDatatypes);

			// Enrich type promotions with reflexive mappings
			new HashSet<>(typePromotions.values()).forEach(x -> typePromotions.put(x, x));
			
			// Add all target types to the rawDatatypeIris
			// rawDatatypeIris.addAll(typePromotions.values());
			
			// Furthermore, if a type has no promotion mapping add it also as a reflexive mapping
			rawDatatypes.stream().filter(dt -> !typePromotions.containsKey(dt))
				.forEach(x -> typePromotions.put(x, x));

			List<String> promotedDatatypes = rawDatatypes.stream()
					.map(iri -> typePromotions.getOrDefault(iri, iri))
					.sorted()
					.collect(Collectors.toList());

			
			// If there is no datatype then use the fallback datatype
			// We assume that the type promoter can handle an empty set of variables
			if (promotedDatatypes.isEmpty()) {
				String fallbackDatatype = effectiveVarToFallbackDatatype.apply(srcVar);
				if (fallbackDatatype != null) {
					promotedDatatypes.add(srcVarName);
				}
			}

			// Apply type remapping if applicable
			// A type that is remapped to null will not have a corresponding column
			if (typeRemap != null) {
				// Pass all currently promoted datatypes through the remap function
				Map<String, String> remaps = promotedDatatypes.stream()
						.collect(Collectors.toMap(
							x -> x,
							x -> typeRemap.apply(x),
							(k1, k2) -> { throw new IllegalStateException("should never happen"); },
							LinkedHashMap::new));

				promotedDatatypes = remaps.values().stream()
						.filter(x -> x != null)
						.distinct()
						.collect(Collectors.toList());
			
				Iterator<Entry<String, String>> it = typePromotions.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> e = it.next();
					String remappedType = remaps.get(e.getValue());
					
					if (remappedType == null) {
						it.remove();
					} else {
						e.setValue(remappedType);
					}					
				}
			}
			
			boolean singleDatatype = promotedDatatypes.size() == 1;

			// Without statistics gracefully assume a column to be nullable
			boolean isNullable = (nullStats == null ? true : nullStats.longValue() > 0) || !singleDatatype;
			
			// Sort datatypes by their suffix in order to obtain a stable order
			Collections.sort(promotedDatatypes, (a, b) -> deriveSuffix(a).compareTo(deriveSuffix(b)));

			SetMultimap<String, String> inverse = Multimaps.invertFrom(Multimaps.forMap(typePromotions), 
				    HashMultimap.<String, String>create());


			for (String datatypeIri : promotedDatatypes) {

				//System.out.println("Processing datatypeIri: " + datatypeIri);
				
				// String castDatatypeIri = typePromotions.getOrDefault(datatypeIri, datatypeIri);
				
				// TODO Resolve name clashes such as rdf:type - custom:type
				String baseName = singleDatatype
						? srcVarName
						: srcVarName + "_" + deriveSuffix(datatypeIri);
				
				Var tgtVar = Var.alloc(baseName);
				
				// For each source datatype create the mapping to the promoted type
				DecisionTreeSparqlExpr dt = new DecisionTreeSparqlExpr();
				for (String srcDtIri : inverse.get(datatypeIri)) {

					if (!srcDtIri.equals(datatypeIri)) {
						
						
						ExprRewrite typeConversion = effectiveTypeConversionSupplier.apply(srcDtIri, datatypeIri);
						
						dt.getRoot()
							.getOrCreateInnerNode(null, E_SerializableIdentity.wrap(
									createDatatypeCheck(srcExprVar, srcDtIri)))				
							.getOrCreateLeafNode(NodeValue.TRUE.asNode())
								.setValue(E_SerializableIdentity.wrap(typeConversion.rewrite(srcExprVar)));
	
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
	
	public static ExprRewrite defaultTypeConversionSupplier(String srcDatatypeIri, String tgtDatatypeIri) {
		return arg -> new E_Function(tgtDatatypeIri, new ExprList(arg));
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
