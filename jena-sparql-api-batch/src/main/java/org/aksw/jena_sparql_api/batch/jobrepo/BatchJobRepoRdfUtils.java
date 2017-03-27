package org.aksw.jena_sparql_api.batch.jobrepo;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

/**
 * Workflow vocabulary
 *
 * @author raven
 *
 */
class WFV {
    public static final String ns = "http://jsa.aksw.org/batch/Workflow";

    public static final Resource Workflow = ResourceFactory.createResource(ns + "Workflow");
    public static final Property content = ResourceFactory.createProperty(ns + "content");
}

class DCAT {
    public static final String ns = "http://www.w3.org/ns/dcat#";

    public static final Property mediaType = ResourceFactory.createProperty(ns + "mediaType");
}

public class BatchJobRepoRdfUtils {

    public static void register(Model model, String uri, String data, String mediaType) {
        Resource s = model.createResource(uri);
        Literal d = model.createLiteral(data);
        Literal mt = model.createLiteral(mediaType);

        model.add(s, RDF.type, WFV.Workflow);
        model.add(s, DCTerms.format, mt);
        model.add(s, WFV.content, d);
        //model.add(s, DCTerms.)
        // creator,
        //model.add
    }

}
