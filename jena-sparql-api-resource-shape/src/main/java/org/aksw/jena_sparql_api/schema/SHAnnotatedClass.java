package org.aksw.jena_sparql_api.schema;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.vocabulary.SH;

/**
 * A helper view for classes for which shacl NodeShape 'annotations' exist
 * and as such these classes appear as values of sh:targetClass properties.
 * Eases navigation to the set of related node shapes
 *
 * @author raven
 *
 */
@ResourceView
public interface SHAnnotatedClass
    extends Resource
{
    @Inverse
    @Iri(SH.NS + "targetClass")
    Set<NodeSchemaFromNodeShape> getNodeShapes();
}
