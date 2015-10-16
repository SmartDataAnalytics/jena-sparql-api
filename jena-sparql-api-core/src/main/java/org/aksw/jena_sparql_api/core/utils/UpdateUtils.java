package org.aksw.jena_sparql_api.core.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.modify.request.UpdateWithUsing;
import com.hp.hpl.jena.update.Update;

public class UpdateUtils {

    public static String getWithIri(Update update) {
        Node with = update instanceof UpdateWithUsing
                ? ((UpdateWithUsing)update).getWithIRI()
                : null;

        String result = with == null ? null : with.toString();

        return result;
    }

}
