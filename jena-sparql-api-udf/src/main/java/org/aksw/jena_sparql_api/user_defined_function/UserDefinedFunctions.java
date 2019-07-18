package org.aksw.jena_sparql_api.user_defined_function;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.function.user.UserDefinedFunctionFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDefinedFunctions {
	private static final Logger logger = LoggerFactory.getLogger(UserDefinedFunctions.class);  
	
	public static ExtendedIterator<UserDefinedFunctionResource> listUserDefinedFunctions(Model model) {
		return model.listSubjectsWithProperty(RDF.type, UdfVocab.UserDefinedFunction)
				.mapWith(x -> x.as(UserDefinedFunctionResource.class));
	}
	
	public static void load(Model model, Set<String> activeProfiles) {
		UserDefinedFunctionFactory f = UserDefinedFunctionFactory.getFactory();
		
		List<UserDefinedFunctionResource> fns = listUserDefinedFunctions(model).toList();
		for(UserDefinedFunctionResource fn : fns) {
			resolveUdf(f, fn, activeProfiles);
		}
	}
	
	public static void resolveUdf(
			UserDefinedFunctionFactory f,
			UserDefinedFunctionResource fn,
			Set<String> activeProfiles)
	{
		String fnIri = fn.getURI();
		UserDefinedFunctionDefinition fnUdfd = f.get(fnIri);

		if(fnUdfd == null) {
			
				// First check which of the udf definitions are active under the given profiles
				// If there are multiple ones, raise an exception with the conflicts
				List<UdfDefinition> activeUdfs = new ArrayList<>();
				for(UdfDefinition def : fn.getDefinitions()) {
					Set<Resource> requiredProfiles = def.getProfiles();
					Set<String> requiredProfileIris = requiredProfiles.stream()
							.filter(RDFNode::isURIResource)
							.map(Resource::getURI)
							.collect(Collectors.toSet());;
					
					Set<String> overlap = Sets.intersection(requiredProfileIris, activeProfiles);
					if(requiredProfiles.isEmpty() || !overlap.isEmpty()) {
						activeUdfs.add(def);
					}
				}
	
				if(activeUdfs.size() != 1) {
					throw new RuntimeException("Expected exactly 1 definition for " + fnIri + "; got: " + activeUdfs);
				}
				
				
				UdfDefinition activeUdf = Iterables.getFirst(activeUdfs, null);

				Resource ra = activeUdf.getAliasFor();

				if(activeUdf.mapsToPropertyFunction()) {
					System.out.println("Mapped pfn");
				} else if(ra != null) {
					UserDefinedFunctionResource alias = ra.as(UserDefinedFunctionResource.class);
					if(alias != null) {
						resolveUdf(f, alias, activeProfiles);
						
						String iri = alias.getURI();
						// Try to resolve the definition
						UserDefinedFunctionDefinition udfd = f.get(iri);
						if(udfd == null) {
							throw new RuntimeException("Could not resolve " + iri);						
						}
						
						//UserDefinedFunctionResource udf = alias.as(UserDefinedFunctionResource.class);
						//UserDefinedFunctionDefinition ud = udf.toJena();
						
						f.add(fnIri, udfd.getBaseExpr(), udfd.getArgList());							
					}
				} else {
					UserDefinedFunctionDefinition udfd = UdfDefinition.toJena(fnIri, activeUdf);
					
					logger.debug("Registering " + udfd);
					
					f.add(udfd.getUri(), udfd.getBaseExpr(), udfd.getArgList());
				}
					
		}
	}
}
