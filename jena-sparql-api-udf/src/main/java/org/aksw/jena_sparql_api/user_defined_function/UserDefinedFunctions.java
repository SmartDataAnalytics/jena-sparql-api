package org.aksw.jena_sparql_api.user_defined_function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.function.user.UserDefinedFunctionFactory;
import org.apache.jena.sparql.util.ExprUtils;
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

    public static void registerAll(Map<String, UserDefinedFunctionDefinition> map) {
        UserDefinedFunctionFactory f = UserDefinedFunctionFactory.getFactory();

        for(UserDefinedFunctionDefinition udfd : map.values()) {
            f.add(udfd.getUri(), udfd.getBaseExpr(), udfd.getArgList());
        }

    }

    public static Map<String, UserDefinedFunctionDefinition> load(Model model, Set<String> activeProfiles) {
        Map<String, UserDefinedFunctionDefinition> result = new LinkedHashMap<>();

        List<UserDefinedFunctionResource> fns = listUserDefinedFunctions(model).toList();
        for(UserDefinedFunctionResource fn : fns) {
            resolveUdf(result, fn, activeProfiles);
        }

        return result;
    }

    public static String forceIri(Resource r) {
        String result = r.isURIResource()
                ? r.getURI()
                : "_:" + r.getId().getBlankNodeId().getLabelString();

        Objects.requireNonNull(result, "Could not craft IRI from " + r);

        return result;
    }

    public static void resolveUdf(
            Map<String, UserDefinedFunctionDefinition> result,
            //UserDefinedFunctionFactory f,
            UserDefinedFunctionResource fn,
            Set<String> activeProfiles)
    {
        String fnIri = forceIri(fn);

        UserDefinedFunctionDefinition fnUdfd = result.get(fnIri);

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

            if(activeUdfs.isEmpty()) {
                logger.warn("User defined function " + fnIri + " has no candidate for profiles " + activeProfiles);
            } else if(activeUdfs.size() > 1) {
                throw new RuntimeException("Expected exactly 1 definition for " + fnIri + "; got: " + activeUdfs);
            } else {

                UdfDefinition activeUdf = Iterables.getFirst(activeUdfs, null);

                // Resolve alias references
                Resource ra = activeUdf.getAliasFor();

                if(activeUdf.mapsToPropertyFunction()) {
                    logger.debug("Mapped property function: " + activeUdf + ", aliasFor: " + ra);
                    UserDefinedFunctionDefinition ud = new UserDefinedFunctionDefinition(fnIri,
                            ExprUtils.parse("<" + fnIri + ">(?s)"),
                            Arrays.asList(Vars.s));

                    result.put(ud.getUri(), ud);
                } else if(ra != null) {
                    UserDefinedFunctionResource alias = ra.as(UserDefinedFunctionResource.class);
                    if(alias != null) {
                        resolveUdf(result, alias, activeProfiles);

                        String iri = forceIri(alias);
                        // Try to resolve the definition
                        // TODO Possibly try to resolve against Jena's function registry
                        UserDefinedFunctionDefinition udfd = result.get(iri);
                        if(udfd == null) {
                            logger.warn("Could not resolve " + iri);
                            //throw new RuntimeException("Could not resolve " + iri);
                        } else {

                            //UserDefinedFunctionResource udf = alias.as(UserDefinedFunctionResource.class);
                            UserDefinedFunctionDefinition ud = new UserDefinedFunctionDefinition(fnIri, udfd.getBaseExpr(), udfd.getArgList());

                            result.put(ud.getUri(), ud);
                            //f.add(fnIri, udfd.getBaseExpr(), udfd.getArgList());
                        }
                    }
                } else {
                    UserDefinedFunctionDefinition udfd = UdfDefinition.toJena(fnIri, activeUdf);

                    logger.debug("Registering " + udfd);

                    result.put(udfd.getUri(), udfd);
                }
            }
        }
    }
}
