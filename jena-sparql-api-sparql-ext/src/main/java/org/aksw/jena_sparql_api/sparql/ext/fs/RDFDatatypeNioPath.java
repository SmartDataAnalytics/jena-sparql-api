package org.aksw.jena_sparql_api.sparql.ext.fs;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.vocabulary.XSD;

/**
 * A datatype for NIO Paths.
 * Not implemented yet.
 * 
 * @author raven
 *
 */
public class RDFDatatypeNioPath
	extends BaseDatatype
{
	// Hijacking of XSD namespace for convenience - may be changed though
	public static final String IRI = XSD.NS + "path";
	
	public RDFDatatypeNioPath(String uri) {
		super(uri);
	}

	@Override
	public String unparse(Object value) {
		return null;
	}
	
	@Override
	public Object parse(String lexicalForm) throws DatatypeFormatException {
		// TODO Auto-generated method stub
		return super.parse(lexicalForm);
	}
	
}
