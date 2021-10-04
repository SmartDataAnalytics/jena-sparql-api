package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.nio.charset.StandardCharsets;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;


public class ConceptManagerImpl
    implements ConceptManager
{
    protected ClassRelationModel classModel;

    public ConceptManagerImpl() {
        super();
        this.classModel = ModelFactory.createDefaultModel().createResource().as(ClassRelationModel.class);
    }

    @Override
    public Node getOrCreate(UnaryRelation concept) {

        String queryStr = concept.toQuery().toString();;
        HashCode hashCode = Hashing.sha256().hashString(queryStr, StandardCharsets.UTF_8);
        String hashStr = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());

        Node node = NodeFactory.createURI("urn:" + hashStr);

        ClassRelationModel result = classModel.getClassModel().computeIfAbsent(node,
                n -> {
                    ClassRelationModel r = classModel.getModel().createResource().as(ClassRelationModel.class);
                    r.setExpression(queryStr);
                    return r;
                });
        System.out.println(result.getExpression());

        return result.asNode();
    }


}
