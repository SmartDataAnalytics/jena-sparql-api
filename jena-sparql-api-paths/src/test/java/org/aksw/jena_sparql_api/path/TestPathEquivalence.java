package org.aksw.jena_sparql_api.path;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.path.core.PathOpsNode;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePPath;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.PathParser;
import org.junit.Assert;
import org.junit.Test;

public class TestPathEquivalence {

    /** Test to ensure that equality tests work as expected */
    @Test
    public void testPropertyPath() {
        org.apache.jena.sparql.path.Path p1p = PathParser.parse("rdfs:subClassOf*", PrefixMapping.Extended);
        Node p1n = NodeFactory.createLiteralByValue(p1p, RDFDatatypePPath.INSTANCE);
        Path<Node> p1 = PathOpsNode.newAbsolutePath().resolve(p1n);

        org.apache.jena.sparql.path.Path p2p = PathParser.parse("rdfs:subClassOf*", PrefixMapping.Extended);
        Node p2n = NodeFactory.createLiteralByValue(p2p, RDFDatatypePPath.INSTANCE);
        Path<Node> p2 = PathOpsNode.newAbsolutePath().resolve(p2n);

        Assert.assertEquals(p1p, p2p);
        Assert.assertEquals(p1n, p2n);
        Assert.assertEquals(p1, p2);
    }

    @Test
    public void testBlankNodeInPath() {
        Node bn = NodeFactory.createBlankNode();
        Path<Node> expected = PathOpsNode.newAbsolutePath().resolve(bn);
        String str = expected.toString();
        Path<Node> actual = PathOpsNode.get().fromString(str);

        Assert.assertEquals(expected, actual);
    }

}
