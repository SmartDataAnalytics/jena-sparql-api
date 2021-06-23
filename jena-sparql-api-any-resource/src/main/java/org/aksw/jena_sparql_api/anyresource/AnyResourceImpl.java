package org.aksw.jena_sparql_api.anyresource;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class AnyResourceImpl
    extends ResourceImpl
    implements AnyResource
{
    public AnyResourceImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static final Implementation factory = new Implementation() {
        @Override
        public boolean canWrap(Node n, EnhGraph eg) {
            return true;
        }

        @Override
        public EnhNode wrap(Node n, EnhGraph eg) {
            return new AnyResourceImpl(n,eg);
        }
    };
};