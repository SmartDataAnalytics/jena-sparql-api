package org.aksw.jena_sparql_api.mapper.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.core.LookupServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.mapper.AccBestLiteral;
import org.aksw.jena_sparql_api.mapper.BestLiteralConfig;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.util.iri.PrefixUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.model.PrefixMapAdapter;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import io.reactivex.rxjava3.core.Flowable;

public class LabelUtils {


    public static Node findBestLiteral(RDFNode rdfNode, BestLiteralConfig config) {
        Node result = null;

        AccBestLiteral acc = new AccBestLiteral(config);

//        Context context = ARQ.getContext().copy() ;
//        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
//        FunctionEnv env = new ExecutionContext(context, null, null, null) ;

        Node s = rdfNode.asNode();
        if (rdfNode.isResource()) {
            Resource r = rdfNode.asResource();

            // If no predicates are given in best literal config then iterate
            // all predicates of the resource
            List<Node> preds = config.getPredicates();
            if (preds == null) {
                preds = r.listProperties()
                        .mapWith(Statement::getObject)
                        .mapWith(RDFNode::asNode)
                        .toList();
            }

            for (Node p : config.getPredicates()) {
                Property prop = new PropertyImpl(p, (EnhGraph)null);

                List<Node> os = r.listProperties(prop)
                        .mapWith(Statement::getObject)
                        .mapWith(RDFNode::asNode)
                        .toList();
                for(Node o : os) {
                    Binding binding = BindingFactory.builder()
                        .add(config.getSubjectVar(), s)
                        .add(config.getPredicateVar(), p)
                        .add(config.getObjectVar(), o)
                        .build();

                    acc.accumulate(binding, null);
                }
            }

            result = Optional.ofNullable(acc.getValue()).map(NodeValue::asNode).orElse(null);
        }

        if (result == null) {
            result = s;
        }

        return result;
    }

    /**
     * A basic lookup service for labels that maps Nodes to Strings.
     * If there is no string for a node, the resulting map will not have an entry
     * for that node.
     *
     *
     * @param conn
     * @param labelProperty
     * @param prefixMapping
     * @return
     */
    public static LookupService<Node, String> getLabelLookupService(
            SparqlQueryConnection conn,
            Property labelProperty,
            PrefixMapping prefixMapping) {

        BinaryRelation labelRelation = BinaryRelationImpl.create(labelProperty);
        return LookupServiceUtils.createLookupService(conn, labelRelation)
              .partition(10)
              .cache()
              .mapValues(LabelUtils::getLabelFromLookup);
    }

    public static String getLabelFromLookup(Node node, List<Node> results) {
        Node labelNode = !results.isEmpty()
                ? results.get(0)
                : node;

        String  label = deriveLabelFromNode(labelNode, null, null);

        return label;
    }

    /**
     * A method similar to {@link NodeFmtLib#displayStr(Node)} however it
     * accepts a {@link PrefixMapping} instead of a {@link PrefixMap}.
     *
     * @param node
     * @param prefixMapping
     * @return
     */
    public static String str(Node node, PrefixMapping prefixMapping) {
        PrefixMap pm = prefixMapping == null
                ? null
                : new PrefixMapAdapter(prefixMapping);

        String result = node == null
                ? "(null)"
                : NodeFmtLib.str(node, pm);

        return result;

    }

    /**
     * An wrapper for {@link #getLabels(Collection, Function, LookupService, PrefixMapping)} that attaches
     * the obtained labels to resources
     *
     * @param <T>
     * @param cs
     * @param nodeFunction
     * @param labelService
     * @param prefixes
     */
    public static <T extends RDFNode, C extends Iterable<T>> C enrichWithLabels(
            C cs,
            Function<? super T, ? extends Node> nodeToLabel,
            LookupService<Node, String> labelService) {
        Map<T, String> labelMap = getLabels(cs, nodeToLabel, labelService);
        // Map<T, String> labelMap = labelService.fetchMap(cs);

        for (Entry<T, String> e : labelMap.entrySet()) {
            RDFNode rdfNode = e.getKey();
            String label = e.getValue();
            if (rdfNode.isResource()) {
                Resource k = rdfNode.asResource();
                ResourceUtils.setLiteralProperty(k, RDFS.label, label);
            }
        }

        return cs;
    }


    public static String deriveLabelFromIri(String iriStr, PrefixMapping pm) {
        String result = null;
        if (pm != null) {
            Entry<String, String> entry = PrefixUtils.findLongestPrefix(pm, iriStr);
            if (entry != null) {
                String localName = iriStr.substring(entry.getValue().length());
                result = entry.getKey() + ":" + localName;
            }
        }

        if (result == null) {
            result = deriveLabelFromIri(iriStr);
        }

        return result;
    }

    public static String deriveLabelFromIri(String iriStr) {
        String result;

        // There are fragments such as #this #self #me #service which are not useful as a label
        // If there are less-than-equal n characters after a hash use a slash as the splitpoint
        int n = 6;
        int lastIdx = iriStr.length() - 1;
        int slashIdx = iriStr.lastIndexOf('/');
        int hashIdx = iriStr.lastIndexOf('#');
        if ((lastIdx - hashIdx) <= n && slashIdx < hashIdx && slashIdx != -1) {
            result = iriStr.substring(slashIdx + 1);
        } else {
            result = SplitIRI.localname(iriStr);
        }

        return result;
    }

//    public static String deriveLabelFromIri(String iriStr) {
//
//        String result;
//        for(;;) {
//            // Split XML returns invalid out-of-bound index for <http://dbpedia.org/resource/Ada_Apa_dengan_Cinta%3>
//            // This is what Node.getLocalName does
//            int idx = Util.splitNamespaceXML(iriStr);
//            result = idx == -1 || idx > iriStr.length() ? iriStr : iriStr.substring(idx);
//            if(result.isEmpty() && !iriStr.isEmpty() && idx != -1) {
//                iriStr = iriStr.substring(0, iriStr.length() - 1);
//                continue;
//            } else {
//                break;
//            }
//        };
//        return result;
//    }


    /**
     * An alternative approach where the label is stored in a simple wrapper object
     *
     * @param <T>
     * @param cs
     * @param itemToLabel
     * @return
     */
    public static <T> Collection<Labeled<T>> wrapWithLabel(Collection<T> cs, Function<? super T, ? extends String> itemToLabel) {
        Collection<Labeled<T>> result = cs.stream()
                .map(item -> new LabeledImpl<T>(item, itemToLabel.apply(item)))
                .collect(Collectors.toList());

        return result;
    }



    /**
     * Formats a node to a generally non-parseable string w.r.t. a given prefix mapping.
     * For example, escaping of double quotes for literals is lost in the output
     *
     * TODO Turn into a class; add option to show/hide language; show/hide quotes; use lexical form or Object.toString; etc
     *
     * This method is similar to {@link NodeFmtLib#str(Node)} however it always
     * picks the longest prefix for the datatype IRI.
     *
     * @param node
     * @param prefixMapping
     * @return
     */
    public static String formatLiteralNode(Node node, PrefixMapping prefixMapping) {
        String result;
        if (node.isLiteral()) {
            Object obj = node.getLiteralValue();

            String baseStr = obj instanceof Number
                    ? Objects.toString(obj)
                    : node.getLiteralLexicalForm();

            String dtIri = node.getLiteralDatatypeURI();
            String dtPart = null;

            // Hide string / langString datatypes
            boolean showDatatype = dtIri != null && !(obj instanceof String);

            if(showDatatype) {
                Entry<String, String> prefixToIri = prefixMapping == null
                    ? null
                    : PrefixUtils.findLongestPrefix(prefixMapping, dtIri);

                dtPart = prefixToIri != null
                    ? prefixToIri.getKey() + ":" + dtIri.substring(prefixToIri.getValue().length())
                    : "<" + dtIri + ">";
            }

            result = showDatatype
                    ? "\"" + baseStr + "\"" + (dtPart == null ? "" : "^^" + dtPart)
                    : baseStr;

        } else {
            result = Objects.toString(node);
        }

        return result;
    }


//    public static <T> LookupService<T, String> createLookupServiceForLabels(
//            Function<? super T, ? extends Node> nodeFunction,
//            LookupService<Node, String> labelService,
//            PrefixMapping iriPrefixes,
//            PrefixMapping literalPrefixes
//    ) {
//        return cs -> Flowable.fromIterable(getLabels(cs, nodeFunction, labelService, iriPrefixes, literalPrefixes).entrySet());
//    }

    /**
     * Wrap a lookup service such that for every node for which no label could be obtained one
     * is derived from the node itself.
     *
     * @param labelService
     * @param iriPrefixes
     * @param literalPrefixes
     * @return
     */
    public static LookupService<Node, String> createLookupServiceForLabels(
            LookupService<Node, String> labelService,
            PrefixMapping iriPrefixes,
            PrefixMapping literalPrefixes
    ) {
        return cs -> Flowable.fromIterable(getLabels(
                    cs, Function.identity(), labelService,
                    iriPrefixes, literalPrefixes
                ).entrySet());
    }


    public static String getOrDeriveLabel(RDFNode rdfNode) {
        return getOrDeriveLabel(rdfNode, BestLiteralConfig.fromProperty(RDFS.label));
    }

    public static String getOrDeriveLabel(RDFNode rdfNode, BestLiteralConfig bestLiteralConfig) {
        return getOrDeriveLabel(rdfNode, bestLiteralConfig, null, null);
    }


    /**
     * Attempt to read the label from given property. If this does not yield a label
     * fall back to deriving a label from the given node itself.
     *
     * @param rdfNode
     * @param labelProperty
     * @param iriPrefixes
     * @param literalPrefixes
     * @return
     */
    public static String getOrDeriveLabel(
            RDFNode rdfNode,
            BestLiteralConfig bestLiteralConfig,
            PrefixMapping iriPrefixes,
            PrefixMapping literalPrefixes) {

        Node tmp = rdfNode.isResource()
                ? findBestLiteral(rdfNode, bestLiteralConfig)
                : null;

        Node labelNode = tmp != null
                ? tmp
                : rdfNode.asNode();

        String result = deriveLabelFromNode(labelNode, iriPrefixes, literalPrefixes);

        return result;
    }


    public static String deriveLabelFromNode(Node node, PrefixMapping iriPrefixes, PrefixMapping literalPrefixes) {
        String result = node == null || NodeUtils.nullUriNode.equals(node)
            ? "(null)"
            : node.isURI()
                ? deriveLabelFromIri(node.getURI(), iriPrefixes)
                : formatLiteralNode(node, literalPrefixes);

        return result;
    }


    /**
     * Fetch labels for the given objects (e.g. RDFNodes) by performing
     * lookups with the corresponding Node objects.
     *
     *
     * @param <T>
     * @param cs
     * @param nodeFunction
     * @param labelService
     * @return
     */
    public static <T> Map<T, String> getLabels(
            Iterable<T> cs,
            Function<? super T, ? extends Node> nodeFunction,
            LookupService<Node, String> labelService) {
        return getLabels(cs, nodeFunction, labelService, null, null);
    }

    /**
     * A variant of getLabels where in cases where no label could be obtained
     * one is derived w.r.t. given prefixes.
     *
     * @param <T>
     * @param cs
     * @param nodeFunction
     * @param labelService
     * @param iriPrefixes
     * @param literalPrefixes
     * @return
     */
    public static <T> Map<T, String> getLabels(
            Iterable<T> cs,
            Function<? super T, ? extends Node> nodeFunction,
            LookupService<Node, String> labelService,
            PrefixMapping iriPrefixes,
            PrefixMapping literalPrefixes) {
//        Multimap<Node, T> index = Multimaps.index(cs, nodeFunction::apply);
        Multimap<Node, T> index = Multimaps.index(cs, item ->
            Optional.<Node>ofNullable(nodeFunction.apply(item)).orElse(NodeUtils.nullUriNode));

        Set<Node> s = index.keySet().stream().filter(Node::isURI).collect(Collectors.toSet());
        Map<Node, String> map = labelService.fetchMap(s);

        Function<Node, String> determineLabel = k -> {
            String r = map.get(k);
            if (r == null) {
                r = deriveLabelFromNode(k, iriPrefixes, literalPrefixes);
            }
            return r;
        };

        Map<T, String> result =
            index.entries().stream().map(
            e -> Maps.immutableEntry(e.getValue(), determineLabel.apply(e.getKey())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }

}
