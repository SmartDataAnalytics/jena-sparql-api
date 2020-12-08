package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.jena.datatypes.RDFDatatype;

import com.google.common.base.Converter;

/**
 * A converter between Objects and lexical forms (Strings) via {@link RDFDatatype}.
 *
 * @author raven
 *
 */
public class ConverterFromObjectToLexicalFormViaRDFDatatype
    extends Converter<Object, String>
{
    protected RDFDatatype rdfDatatype;

    public ConverterFromObjectToLexicalFormViaRDFDatatype(RDFDatatype rdfDatatype) {
        super();
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    protected String doForward(Object value) {
        String result = rdfDatatype.unparse(value);
        return result;
    }

    @Override
    protected Object doBackward(String lexicalForm) {
        Object result = rdfDatatype.parse(lexicalForm);
        return result;
    }
}
