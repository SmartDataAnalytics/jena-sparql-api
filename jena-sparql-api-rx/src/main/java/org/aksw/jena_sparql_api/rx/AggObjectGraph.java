package org.aksw.jena_sparql_api.rx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.SetMultimap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;

/**
 * An aggregator whose accumulators create RDF graphs (triples) from the bindings passed to them.
 * At the core this is exactly that of SPARQL construct query, however this class supports an extension:
 *
 * Blank nodes of the template can be remapped via a custom function based on the binding.
 * Template blank nodes that aree not remapped by the function will be mapped to fresh blank nodes
 * on every binding.
 *
 *
 * @author raven
 *
 */
public class AggObjectGraph
    implements Aggregator<Binding, Graph>
// implements Acc<Graph> {
{
    protected Template template;
    /**
     * Mapping of a subset of the bnodes in the template to
     * functions that generate a local id from a binding passed to this accumulator
     */
    protected Map<Node, ? extends Function<? super Binding, ? extends Node>> nodeIdGenMap;

    protected Supplier<Graph> graphSupplier;

    // Accumulator will keep track of the remapping of blank nodes in this set
    protected Set<? extends Node> trackedTemplateNodes;

    public AggObjectGraph(
            Template template,
            Set<? extends Node> trackedTemplateNodes,
            Supplier<Graph> graphSupplier,
            Map<Node, ? extends Function<? super Binding, ? extends Node>> nodeIdGenMap) {
        super();
        this.template = template;
        this.trackedTemplateNodes = trackedTemplateNodes;
        this.graphSupplier = graphSupplier;
        this.nodeIdGenMap = nodeIdGenMap;
    }

    @Override
    public AccObjectGraph createAccumulator() {
        Graph graph = graphSupplier.get();
        return new AccObjectGraph(graph);
    }


    public class AccObjectGraph
        implements Accumulator<Binding, Graph>
    {
        protected Graph graph;
        protected SetMultimap<Node, Node> templateNodeToInsts;

        public AccObjectGraph(Graph graph) {
            super();
            this.graph = graph;
            this.templateNodeToInsts = HashMultimap.create();
        }

        public void accumulate(Binding binding) {
            Map<Node, Node> bnodeMap = new HashMap<>();

            for (Entry<Node, ? extends Function<? super Binding, ? extends Node>> nodeIdGen : nodeIdGenMap.entrySet()) {
                Node templateNode = nodeIdGen.getKey();
                Function<? super Binding, ? extends Node> idGen = nodeIdGen.getValue();

                Node id = idGen.apply(binding);
                bnodeMap.put(templateNode, id);
            }

            for(Triple t : template.getTriples()) {
                Triple newT = TemplateLib.subst(t, binding, bnodeMap);
                if(newT.isConcrete()) {
                    graph.add(newT);
                }
            }

            for (Node templateNode : trackedTemplateNodes) {
                Node remapped;
                if (templateNode.isVariable()) {
                    Var var = (Var)templateNode;
                    remapped = binding.get(var);
                } else {
                    remapped = bnodeMap.get(templateNode);
                }

                if (remapped != null) {
                    templateNodeToInsts.put(templateNode, remapped);
                }
            }
        }

        /**
         * Return for a given node of the template all the instances
         * (either from bindings or from bnode mapping)
         * that were generated in this accumulator
         *
         * @param templateNode
         * @return
         */
        public Set<Node> getTrackedNodes(Node templateNode) {
            Set<Node> result = templateNodeToInsts.get(templateNode);
            return result;
        }

        // @Override
        public Graph getValue() {
            return graph;
        }
    }
}