package org.aksw.jena_sparql_api.batch.cli.main;

import java.util.Iterator;

import org.aksw.jena_sparql_api.utils.ExtendedIteratorClosable;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotReader;

import com.google.common.base.Supplier;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class SupplierExtendedIteratorTriples
    implements Supplier<ExtendedIterator<Triple>>
{
    protected String fileNameOrUrl;
    protected Lang langHint;

    public SupplierExtendedIteratorTriples(String fileNameOrUrl) {
        this(fileNameOrUrl, null);
    }

    public SupplierExtendedIteratorTriples(String fileNameOrUrl, Lang langHint) {
        this.fileNameOrUrl = fileNameOrUrl;
        this.langHint = langHint;
    }

    @Override
    public ExtendedIterator<Triple> get() {
        TypedInputStream tis = RDFDataMgr.open(fileNameOrUrl);
        Lang lang = RDFDataMgr.determineLang(fileNameOrUrl, null, langHint);
        String base = tis.getBaseURI();

        Iterator<Triple> itTriple = RiotReader.createIteratorTriples(tis, lang, base);
        ExtendedIterator<Triple> result = ExtendedIteratorClosable.create(itTriple, tis);

        return result;
    }

}
