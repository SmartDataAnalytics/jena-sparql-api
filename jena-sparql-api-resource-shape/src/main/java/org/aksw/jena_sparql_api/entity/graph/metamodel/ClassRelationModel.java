package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.KeyIri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ClassRelationModel
    extends Resource
{
    // String setExpression

    @IriNs("eg")
    String getExpression();
    ClassRelationModel setExpression(String value);


    /** Get the class' enumeration of items if applicable*/
    // @Iri("urn:member")
    // Set<Node> getEnumeration();

    @IriNs("eg")
    @KeyIri("urn:resource")
    // @ValueIri("urn:value")
    Map<Node, ClassRelationModel> getClassModel();

    default ClassRelationModel getOrCreateClassModel(Node key) {
        ClassRelationModel result = getClassModel()
                .computeIfAbsent(key, k -> getModel().createResource().as(ClassRelationModel.class));

        return result;
    }


}
