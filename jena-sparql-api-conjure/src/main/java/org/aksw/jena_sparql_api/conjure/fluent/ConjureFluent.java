package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

public interface ConjureFluent {
    Op getOp();

    // Not sure if this is the best place
    // hdtHeader is a modifier for a datarefUrl
    ConjureFluent hdtHeader();

    ConjureFluent cache();

    /**
     * Construct a new dataset from a sequence of construct queries
     *
     * @param queryStrs
     * @return
     */
    ConjureFluent construct(Collection<String> queryStrs);
    //ConjureFluent construct(Collection<Query> queryStrs);

    default ConjureFluent construct(String queryStr) {
        return construct(Collections.singleton(queryStr));
    }


    ConjureFluent stmts(Collection<String> stmtStrs);
    //ConjureFluent construct(Collection<Query> queryStrs);

    default ConjureFluent stmt(String stmtStr) {
        return construct(Collections.singleton(stmtStr));
    }


    ConjureFluent update(String updateRequest);
    ConjureFluent views(String ... queryStrs);
    ConjureFluent views(Collection<Query> queries);


    ConjureFluent set(String ctxVar, String selector, String path);

    default ConjureFluent construct(Query query) {
        return construct(query.toString());
    }

    default ConjureFluent update(UpdateRequest updateRequest) {
        return update(updateRequest.toString());
    }

    // We could create the queries programmatically in a util function
    // But we will validated them anyway with the parser

    default ConjureFluent ofProperty(String p) {
        return construct("CONSTRUCT WHERE { ?s <" + p + "> ?o");
    }


    default ConjureFluent everthing() {
        return construct(QLib.everything());
    }


    default ConjureFluent tripleCount() {
        return construct(QLib.tripleCount());
    }

    default ConjureFluent compose(Function<? super ConjureFluent, ? extends ConjureFluent> composer) {
        ConjureFluent result = composer.apply(this);
        return result;
    }
}
