package org.aksw.jena_sparql_api.rdf.model.ext.dataset.api;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.LiteralInDatasetImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Literal;

public interface LiteralInDataset
    extends Literal, RDFNodeInDataset
{
    @Override
    default LiteralInDataset asLiteral() {
        return this;
    }

    @Override
    LiteralInDatasetImpl inDataset(Dataset other);
}
