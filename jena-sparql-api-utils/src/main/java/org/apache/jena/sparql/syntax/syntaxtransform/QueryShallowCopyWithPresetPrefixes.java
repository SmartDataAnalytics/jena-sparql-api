package org.apache.jena.sparql.syntax.syntaxtransform;

import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps.QueryShallowCopy;

/**
 * A version of QueryShallowCopy without copying the prefixes.
 *
 * QueryShallowCopy is only package visible - hence this hack
 *
 */
public class QueryShallowCopyWithPresetPrefixes
    extends QueryShallowCopy
{
    protected PrefixMapping pmap;

    public QueryShallowCopyWithPresetPrefixes(PrefixMapping pmap) {
        super();
        this.pmap = pmap;
    }

    @Override
    public void visitPrologue(Prologue prologue) {
        if (pmap != null) {
            newQuery.setPrefixMapping(pmap);
        }
    }

    public static Query shallowCopy(Query query, PrefixMapping pmap) {
        QueryShallowCopy copy = new QueryShallowCopyWithPresetPrefixes(pmap);
        query.visit(copy);
        Query q2 = copy.newQuery;
        return q2;
    }
}
