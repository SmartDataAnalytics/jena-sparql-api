// Not used

//package org.aksw.jena_sparql_api.algebra.analysis;
//
//import java.util.Collections;
//import java.util.Map;
//import java.util.Set;
//
//import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
//import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
//import org.aksw.jena_sparql_api.user_defined_function.UdfVocab;
//import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctions;
//import org.aksw.jena_sparql_api.utils.QueryUtils;
//import org.apache.jena.query.Query;
//import org.apache.jena.query.QueryExecution;
//import org.apache.jena.query.QueryFactory;
//import org.apache.jena.query.QuerySolution;
//import org.apache.jena.query.ResultSet;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.RDFNode;
//import org.apache.jena.rdfconnection.RDFConnection;
//import org.apache.jena.rdfconnection.RDFConnectionRemote;
//import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
//import org.apache.jena.riot.RDFDataMgr;
//import org.apache.jena.riot.WebContent;
//import org.apache.jena.sparql.algebra.Transformer;
//import org.apache.jena.sparql.expr.ExprTransform;
//import org.apache.jena.sparql.function.user.ExprTransformExpand;
//import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
//
//import com.google.common.collect.HashBasedTable;
//import com.google.common.collect.Table;
//
//public class ProfileProber {
//	
//	public static String pickBestProfile(Table<String, String, Boolean> featureMatrix) {
//		//featureMatrix.
//		return null;
//	}
//
//	/**
//	 * Profile IRI -> Function IRI -> Boolean indicator of support
//	 * 
//	 * @param model
//	 * @return
//	 */
//	public static Table<String, String, Boolean> probe(Model model, RDFConnection conn) {
//		Table<String, String, Boolean> result = HashBasedTable.create();
//		
//		Set<RDFNode> availableProfiles = model.listObjectsOfProperty(UdfVocab.profile).toSet();
//		
//		String featureIRI = ExprTransformVirtualBnodeUris.bidOfFnIri;
//
//		// The query is deliberately compatible with
//		// SPARQL 1.0 only in an attempt to cover as many systems as possible
//		//String baseQueryStr =  "SELECT ?o1 ?o2 { ?s a ?o1 FILTER(isBLANK(?o1)) OPTIONAL { ?s a ?o2 FILTER(<" + featureIRI + ">(?o2)) } } LIMIT 1";
//		String baseQueryStr = "SELECT ?o { ?s a ?o FILTER(isBLANK(?o)) FILTER(<" + featureIRI + ">(?o)) } LIMIT 1";
//		Query baseQuery = QueryFactory.create(baseQueryStr);
//		
//		for(RDFNode profile : availableProfiles) {
//			String profileIRI = profile.isURIResource() ? profile.asResource().getURI() : null;
//			if(profileIRI != null) {
//				System.out.println("Profile: " + profileIRI);
//				Map<String, UserDefinedFunctionDefinition> macros = UserDefinedFunctions.load(model, Collections.singleton(profileIRI));
//
//				ExprTransform xform = new ExprTransformExpand(macros);
//				//ExprTransformVirtualBnodeUris xform = new ExprTransformVirtualBnodeUris(macros);
//				Query probeQuery = QueryUtils.rewrite(baseQuery, op -> Transformer.transform(null, xform, op));
//				
//				
//				System.out.println(baseQueryStr);
//				System.out.println(probeQuery);
//
//				Boolean featureSupported = null;
//				try(QueryExecution qe = conn.query(probeQuery)) {
//					ResultSet rs = qe.execSelect();
//					while(rs.hasNext()) {
//						QuerySolution qs = rs.next();
//						RDFNode node = qs.get("o");
//						featureSupported = node != null;
//					}
//				} catch(Exception e) {
//					System.out.println(e);
//					featureSupported = false;
//				}
//				
//				result.put(profileIRI, featureIRI, featureSupported);
//			}
//		}
//		
//		return result;
//	}
//	
////	public static void probe(Collection<? extends UserDefinedFunctionDefinition> u) {
////		
////	}
//	
//	
//	public static void main(String[] args) {
//		Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
//		RDFDataMgrEx.execSparql(model, "udf-inferences.sparql");
//
//		RDFConnection conn = RDFConnectionRemote.create()
//				.destination("http://dbpedia.org/sparql")
//				.acceptHeaderSelectQuery(WebContent.contentTypeResultsXML)
//				.build();
//		
//		Table<String, String, Boolean> table = probe(model, conn);
//		System.out.println(table);
//	}
//}
