package org.aksw.jena_sparql_api.batch;

import com.google.gson.Gson;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.vocabulary.XSD;

class RDFDatatypeJson
    extends BaseDatatype
{
    private Gson gson;

    public RDFDatatypeJson() {
        this(XSD.getURI() + "json");
    }

    public RDFDatatypeJson(String uri) {
        this(uri, new Gson());
    }

    public RDFDatatypeJson(String uri, Gson gson) {
        super(uri);
        this.gson = gson;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = gson.toJson(value);
        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        Object result = gson.fromJson(lexicalForm, Object.class);
        return result;
    }

}