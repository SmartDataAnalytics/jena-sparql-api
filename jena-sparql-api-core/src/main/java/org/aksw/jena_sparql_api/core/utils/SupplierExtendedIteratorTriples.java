package org.aksw.jena_sparql_api.core.utils;

import java.util.Iterator;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.ExtendedIteratorClosable;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.iterator.ExtendedIterator;


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
        ExtendedIterator<Triple> result = createTripleIterator(fileNameOrUrl, langHint);

        return result;
    }


    public static ExtendedIterator<Triple> createTripleIterator(String fileNameOrUrl, Lang langHint) {
        TypedInputStream tis = RDFDataMgr.open(fileNameOrUrl);
        Lang lang = RDFDataMgr.determineLang(fileNameOrUrl, null, langHint);
        String base = tis.getBaseURI();

        Iterator<Triple> itTriple = RDFDataMgr.createIteratorTriples(tis, lang, base);
        ExtendedIterator<Triple> result = ExtendedIteratorClosable.create(itTriple, tis);


        return result;
    }
}
