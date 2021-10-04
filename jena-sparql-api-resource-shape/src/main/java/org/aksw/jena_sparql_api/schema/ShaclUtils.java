package org.aksw.jena_sparql_api.schema;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.vocabulary.SH;


/**
 * Utility methods for working with SHACL.
 * Most prominently provides methods to create Jena paths from shacl paths.
 *
 * @author raven
 *
 */
public class ShaclUtils {

    public static final PathAssembler DEFAULT_PATH_ASSEMBLER = new PathAssembler();

    public static Path assemblePath(Resource r) {
        return DEFAULT_PATH_ASSEMBLER.assemblePath(r);
    }



    /**
     * Function to convert the object of a <pre>sh:path</pre> property to a
     * Jena {@link Path} expression. Supports all cases described in
     * property paths in https://www.w3.org/TR/shacl/#property-paths
     *
     * Does not perform (in-depth) validation. The shapes should be validated
     * before calling this method.
     *
     * @author Claus Stadler
     *
     */
    public static class PathAssembler {

        public Path assemblePath(Resource r) {

            Path result;
            Resource o;
            if ((o = r.getPropertyResourceValue(SH.inversePath)) != null) {
                Path tgt = assemblePath(o);
                if (tgt instanceof P_Link) {
                    result = new P_ReverseLink(((P_Link)tgt).getNode());
                } else {
                    result = PathFactory.pathInverse(tgt);
                }

            } else if ((o = r.getPropertyResourceValue(SH.alternativePath)) != null) {
                List<Path> paths = assembleList(o);
                result = paths.stream().reduce(PathFactory::pathAlt)
                        .orElseThrow(() -> new IllegalStateException("Zero length path encountered"));

            } else if ((o = r.getPropertyResourceValue(SH.zeroOrOnePath)) != null) {
                Path tgt = assemblePath(o);
                result = PathFactory.pathZeroOrOne(tgt);

            } else if ((o = r.getPropertyResourceValue(SH.zeroOrMorePath)) != null) {
                Path tgt = assemblePath(o);
                result = PathFactory.pathZeroOrMore1(tgt);

            } else if ((o = r.getPropertyResourceValue(SH.oneOrMorePath)) != null) {
                Path tgt = assemblePath(o);
                result = PathFactory.pathOneOrMore1(tgt);

            } else if (r.hasProperty(RDF.first)) {
                List<Path> paths = assembleList(r);
                result = paths.stream().reduce(PathFactory::pathSeq)
                        .orElseThrow(() -> new IllegalStateException("Zero length path encountered"));

            } else {
                Node node = r.asNode();
                result = PathFactory.pathLink(node);
            }

            return result;
        }

        /** Assemble a list of paths from an RDF list */
        public List<Path> assembleList(Resource listNode) {
            RDFList list = listNode.as(RDFList.class);
            List<RDFNode> items = list.asJavaList();

            List<Path> result = new ArrayList<>(items.size());
            for (RDFNode item : items) {
                Resource r = item.asResource();
                Path path = assemblePath(r);
                result.add(path);
            }

            return result;
        }
    }




    public static void main(String[] args) {
        String str = "# Example paths taken from https://www.w3.org/TR/shacl/#property-paths\n"
                + "\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX sh: <http://www.w3.org/ns/shacl#>\n"
                + "\n"
                + "ex:a sh:property [ sh:path ex:parent ] .\n"
                + "\n"
                + "ex:b sh:property [ sh:path [ sh:inversePath ex:parent ] ] .\n"
                + "\n"
                + "ex:c sh:property [ sh:path ( ex:parent ex:firstName ) ] .\n"
                + "\n"
                + "ex:d sh:property [ sh:path ( rdf:type [ sh:zeroOrMorePath rdfs:subClassOf ] ) ] .\n"
                + "\n"
                + "ex:e sh:property [ sh:path [ sh:alternativePath ( ex:father ex:mother  ) ] ] .\n";

        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new ByteArrayInputStream(str.getBytes()), Lang.TURTLE);

        List<Resource> nodeShapes = model.listResourcesWithProperty(SH.property).toList();

        for (Resource nodeShape : nodeShapes) {
            Resource x = nodeShape.getPropertyResourceValue(SH.property).getPropertyResourceValue(SH.path);
            Path path = ShaclUtils.assemblePath(x);
            System.out.println(nodeShape + ": " + path);
        }


    }
}
