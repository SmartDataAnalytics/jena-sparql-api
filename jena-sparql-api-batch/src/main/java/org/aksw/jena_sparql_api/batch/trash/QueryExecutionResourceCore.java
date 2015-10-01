package org.aksw.jena_sparql_api.batch.trash;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.batch.step.F_QuadToBinding;
import org.aksw.jena_sparql_api.batch.step.F_TripleToQuad;
import org.aksw.jena_sparql_api.core.QueryExecutionAdapter;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotReader;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;

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

        Iterator<Triple> itTriple = RiotReader.createIteratorTriples(tis, lang, base);

        Function<Triple, Binding> fn = Functions.compose(
                F_QuadToBinding.fn,
                F_TripleToQuad.fn);

        Iterator<Binding> itBinding = Iterators.transform(itTriple, fn);
        QueryIter queryIter = new QueryIterPlainWrapper(itBinding);
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
