package org.aksw.jena_sparql_api.batch.trash;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.batch.step.F_QuadToBinding;
import org.aksw.jena_sparql_api.batch.step.F_TripleToQuad;
import org.aksw.jena_sparql_api.utils.query_execution.QueryExecutionAdapter;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterators;

public class QueryExecutionResourceCore
    extends QueryExecutionAdapter
{
    private String fileNameOrUrl;

    private TypedInputStream tis;

    public QueryExecutionResourceCore(String fileNameOrUrl) {
        this.fileNameOrUrl = fileNameOrUrl;
    }

    @Override
    public ResultSet execSelect() {
        if(tis != null) {
            throw new RuntimeException("Query execution already running");
        }

        tis = RDFDataMgr.open(fileNameOrUrl);
        Lang lang = RDFDataMgr.determineLang(fileNameOrUrl, null, null);
        String base = tis.getBaseURI();

        Iterator<Triple> itTriple = RDFDataMgr.createIteratorTriples(tis, lang, base);

        Function<Triple, Binding> fn = Functions.compose(
                F_QuadToBinding.fn,
                F_TripleToQuad.fn);

        Iterator<Binding> itBinding = Iterators.transform(itTriple, fn);
        QueryIterator queryIter = QueryIterPlainWrapper.create(itBinding);
        List<String> varNames = Arrays.asList("g", "s", "p", "o");
        ResultSet rs = ResultSetFactory.create(queryIter, varNames);
        return rs;
    };

    @Override
    public void abort() {
        close();
    }

    @Override
    public void close() {
        tis.close();
    }
}
