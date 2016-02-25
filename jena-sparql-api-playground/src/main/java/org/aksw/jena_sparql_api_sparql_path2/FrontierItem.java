package org.aksw.jena_sparql_api_sparql_path2;

import scala.Tuple2;

public class FrontierItem<S, V, E>
    extends Tuple2<V, FrontierData<S, V, E>> {
    private static final long serialVersionUID = 6450807270172504356L;

    public FrontierItem(V _1, FrontierData<S, V, E> _2) {
        super(_1, _2);
    }
}