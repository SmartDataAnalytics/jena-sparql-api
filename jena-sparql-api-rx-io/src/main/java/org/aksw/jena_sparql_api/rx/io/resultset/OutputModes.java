package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.Collection;

import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;

/**
 * Utils to derive an {@link OutputMode} from given arguments such as
 * a collection of statements.
 * 
 * @author raven
 *
 */
public class OutputModes {
    /**
     * If the last query is a json query then the mode is json.
     * If there is a construct query the mode is quads.
     * If there is no construct query but a select one, the mode is bindings.
     *
     */
    public static OutputMode detectOutputMode(Collection<? extends SparqlStmt> stmts) {
        OutputMode result = null;
        if (stmts.isEmpty()) {
            result = OutputMode.TRIPLE;
        } else {
            SparqlStmt last = Iterables.getLast(stmts);
            if (last.isQuery()) {
                Query q = last.getQuery();
                if (q.isJsonType()) {
                    result = OutputMode.JSON;
                }
            }


            int tripleCount = 0;
            int quadCount = 0;
            int bindingCount = 0;

            if (result == null) {
                for (SparqlStmt stmt : stmts) {
                    if (stmt.isQuery()) {
                        Query q = stmt.getQuery();

                        if (q.isConstructType()) {
                            if (q.isConstructQuad()) {
                                ++quadCount;
                            } else {
                                ++tripleCount;
                            }
                        } else if (q.isSelectType()) {
                            ++bindingCount;
                        }
                    }
                }

                if (quadCount != 0) {
                    result = OutputMode.QUAD;
                } else if (tripleCount != 0) {
                    result = OutputMode.TRIPLE;
                } else if (bindingCount != 0) {
                    result = OutputMode.BINDING;
                }
            }


            if(result == null) {
                result = OutputMode.TRIPLE;
            }
        }

        return result;
    }


    public static OutputMode determineOutputMode(Lang lang) {
        OutputMode result;
        if (RDFLanguages.isTriples(lang) || RDFLanguages.isQuads(lang)) {
            result = OutputMode.QUAD;
        } else if (ResultSetWriterRegistry.isRegistered(lang)) {
            result = OutputMode.BINDING;
        } else {
            //result = OutputMode.UNKOWN;
            result = null;
        }

        return result;
    }
}
