package org.aksw.jena_sparql_api.batch.jobrepo;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

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
