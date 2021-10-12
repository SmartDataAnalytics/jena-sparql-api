package org.aksw.jena_sparql_api.constraint.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.constraint.api.Contradicting;
import org.aksw.jena_sparql_api.constraint.api.NodeWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.ValueSpaceClassification;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * Track ranges of nodes in their respective value space classifications (vsc).
 *
 * An empty set of ranges is treated as 'contradiction'. This means it is not possible
 *
 * FIXME Use of non-singletons in the 'unknown' value space must be handled as effectively
 * unconstrained
 *
 *
 * @author raven
 *
 */
public class NodeRanges
    implements Contradicting, Cloneable
{
    // Additional (pseudo) value space classifications for unform handingl of IRIs and bnodes
    public static final String VSC_IRI = "ValueSpaceClassification.IRI";
    public static final String VSC_BNODE = "ValueSpaceClassification.BNODE";
    public static final String VSC_TRIPLE = "ValueSpaceClassification.TRIPLE";

    /**
     * A value of null means unconstrained.
     * Object in order to allow for future custom value spaces.
     */
    protected Map<Object, RangeSet<NodeWrapper>> vscToRangeSets = null; // = new HashMap<>();

    protected NodeRanges() {
        super();
    }


    public NodeRanges(Map<Object, RangeSet<NodeWrapper>> vscToRangeSets) {
        super();
        this.vscToRangeSets = vscToRangeSets;
    }


    /**
     * Create an independent copy of this object
     */
    @Override
    public NodeRanges clone() {
        Map<Object, RangeSet<NodeWrapper>> clone = vscToRangeSets.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> TreeRangeSet.create(e.getValue())));
        return new NodeRanges(clone);
    }

    public static NodeRanges create() {
        return new NodeRanges();
    }

    /** Unconstrained mode means that any valid srange is considered enclosed by this one */
    public boolean isUnconstrained() {
        return vscToRangeSets == null;
    }

    @Override
    public boolean isContradicting() {
        return vscToRangeSets != null && vscToRangeSets.isEmpty();
    }

    protected void ensureConstrainedMode() {
        if (vscToRangeSets == null) {
            vscToRangeSets = new HashMap<>();
        }
    }

    public static Object classifyNodeValueSubSpace(Node node) {
        Object result;
        if (node.isURI()) {
            result = VSC_IRI;
        } else if (node.isBlank()) {
            result = VSC_BNODE;
        } else if (node.isNodeTriple()) {
            result = VSC_TRIPLE;
        } else {
            throw new RuntimeException("Unknown term type: " + node);
        }

        return result;
    }

    /** Return some object that acts as a key for a value space. Different value spaces are assumed to be disjoint. */
    public static Object classifyValueSpace(Range<NodeWrapper> range) {
        Object result = null;
        NodeValue lb = range.hasLowerBound() ? range.lowerEndpoint().getNodeValue() : null;
        NodeValue ub = range.hasUpperBound() ? range.upperEndpoint().getNodeValue() : null;

        if (lb != null && ub != null) {
            result = NodeValue.classifyValueOp(lb, ub);

            if (ValueSpaceClassification.VSPACE_NODE.equals(result)) {
                Object a = classifyNodeValueSubSpace(lb.asNode());
                Object b = classifyNodeValueSubSpace(ub.asNode());

                if (!Objects.equals(a, b)) {
                    result = ValueSpaceClassification.VSPACE_DIFFERENT;
                } else {
                    result = a;
                }
            }

        } else if (lb != null) {
            result = lb.getValueSpace();
            if (ValueSpaceClassification.VSPACE_NODE.equals(result)) {
                result = classifyNodeValueSubSpace(lb.asNode());
            }
        } else if (ub != null) {
            result = ub.getValueSpace();
            if (ValueSpaceClassification.VSPACE_NODE.equals(result)) {
                result = classifyNodeValueSubSpace(ub.asNode());
            }
        }

        return result;
    }


    public void add(Range<NodeWrapper> range) {

        if (!isContradicting()) {
            Object vsc = classifyValueSpace(range);

            if (vsc == null) {
                // unconstraint range - treat as noop
            } else if (ValueSpaceClassification.VSPACE_DIFFERENT.equals(vsc) && vscToRangeSets != null) {
                vscToRangeSets.clear();
            } else {
                ensureConstrainedMode();
                RangeSet<NodeWrapper> rangeSet = vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());
                rangeSet.add(range);
            }
        }
    }

    /**
     * Mutate the ranges of this to create the intersection with other
     *
     * @param other
     * @return
     */
    public boolean stateIntersection(NodeRanges other) {
        if (other.isContradicting()) {
            ensureConstrainedMode();
            this.vscToRangeSets.clear();
        }

        // If other is unconstrained then there is nothing to do
        if (!this.isContradicting() && !other.isUnconstrained()) {
            ensureConstrainedMode();
            //for (Entry<Object, RangeSet<ComparableNodeWrapper>> e: vscToRangeSets.entrySet()) {
            for (Iterator<Entry<Object, RangeSet<NodeWrapper>>> it = vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
                Entry<Object, RangeSet<NodeWrapper>> e = it.next();
                Object vsc = e.getKey();
                RangeSet<NodeWrapper> thisRangeSet = e.getValue();

                RangeSet<NodeWrapper> otherRangeSet = other.vscToRangeSets.get(vsc);

                // If there are no other ranges in the value space than the intersection is empty
                if (otherRangeSet == null) {
                    it.remove();
                } else {
                    // Intersection by means of removing the other's complement
                    // https://github.com/google/guava/issues/1825
                    thisRangeSet.removeAll(otherRangeSet.complement());

                    if (thisRangeSet.isEmpty()) {
                        it.remove();
                    }
                }
            }
        }

        return isContradicting();
    }


    /**
     * Add all other ranges to this one
     *
     * @param other
     * @return
     */
    public boolean stateUnion(NodeRanges other) {
        if (this.isUnconstrained() || other.isUnconstrained()) {
            this.vscToRangeSets = null;
        } else {
            ensureConstrainedMode();

            //for (Entry<Object, RangeSet<ComparableNodeWrapper>> e: vscToRangeSets.entrySet()) {
            for (Iterator<Entry<Object, RangeSet<NodeWrapper>>> it = other.vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
                Entry<Object, RangeSet<NodeWrapper>> e = it.next();
                Object vsc = e.getKey();

                RangeSet<NodeWrapper> otherRangeSet = e.getValue();
                RangeSet<NodeWrapper> thisRangeSet = this.vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());

                thisRangeSet.addAll(otherRangeSet);
            }
        }

        return isContradicting();
    }

    public Set<?> getValueSpaces() {
        return vscToRangeSets.keySet();
    }

    public boolean contains(Node node) {
        return contains(NodeValue.makeNode(node));
    }

    public boolean contains(NodeValue nodeValue) {
        Object vsc = nodeValue.getValueSpace();
        NodeWrapper tmp = NodeWrapper.wrap(nodeValue);
        boolean result = Optional.ofNullable(vscToRangeSets.get(vsc)).map(rangeSet -> rangeSet.contains(tmp)).orElse(false);
        return result;
    }

    /** True iff all ranges are singletons; i.e. if this object can be converted into
     * a possible empty enumeration of nodes */
    public boolean isDiscrete() {
        boolean result = vscToRangeSets != null && vscToRangeSets.values().stream()
                .allMatch(RangeUtils::isDiscrete);
        return result;
    }

    public Stream<Node> streamDiscrete() {
        return vscToRangeSets.values().stream().flatMap(rangeSet -> rangeSet.asRanges().stream()
                .map(Range::lowerEndpoint)
                .map(NodeWrapper::getNode));
    }

    /** Returns true if there is exactly one value space with exactly one singleton */
    public boolean isConstant() {
        boolean result = vscToRangeSets != null && vscToRangeSets.size() == 1
                ? RangeUtils.isSingleton(vscToRangeSets.values().iterator().next())
                : false;

        return result;
    }

    /** Always first check {@link #isConstant()} before calling this method */
    public Node getConstant() {
        Node result = vscToRangeSets.values().iterator().next().asRanges().iterator().next().lowerEndpoint().getNode();
        return result;
    }


    /**
     * Return true if this node range subsumes the other one, i.e.
     * this node range is less-or-equal restrictive than the other.
     *
     *
     * @param other
     * @return
     */
    public boolean subsumes(NodeRanges other) {
        boolean result;
        if (other.isUnconstrained()) {
            result = this.isUnconstrained();
        } else {
            if (this.isUnconstrained()) {
                result = true;
            } else {
                result = this.getValueSpaces().containsAll(other.getValueSpaces());

                if (result) {
                    for (Entry<Object, RangeSet<NodeWrapper>> e : this.vscToRangeSets.entrySet()) {
                        Object vsc = e.getKey();
                        RangeSet<NodeWrapper> thisRangeSet = e.getValue();

                        RangeSet<NodeWrapper> otherRangeSet = other.vscToRangeSets.get(vsc);
                        if (otherRangeSet == null) {
                            result = false;
                            break;
                        } else {
                            result = thisRangeSet.enclosesAll(otherRangeSet);
                        }
                    }
                }

            }
        }

        return result;
    }



    public static void main(String[] args) {
        Node iri = NodeFactory.createURI("http://example.org/foo");

        NodeValue a = NodeValue.makeInteger(100);
        NodeValue b = NodeValue.makeInteger(500);
        NodeValue c = NodeValue.makeString("hi");
        NodeValue d = NodeValue.makeDouble(123.4);
        NodeValue e = NodeValue.makeNode(iri);

        NodeRanges nr = NodeRanges.create();
        System.out.println(nr.isUnconstrained());

        nr.add(Range.singleton(NodeWrapper.wrap(iri)));
        nr.add(Range.singleton(NodeWrapper.wrap(a)));
        System.out.println(nr.contains(b));
        System.out.println(nr.contains(c));

        System.out.println(NodeValue.compare(a, d));

        System.out.println(nr.getValueSpaces());

        // System.out.println(nr.is);
        System.out.println("isDiscrete: " + nr.isDiscrete());
        System.out.println("isConstant: " + nr.isConstant());
    }


}
