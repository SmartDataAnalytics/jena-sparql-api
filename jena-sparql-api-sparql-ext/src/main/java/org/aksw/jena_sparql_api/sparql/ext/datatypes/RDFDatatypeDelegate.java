package org.aksw.jena_sparql_api.sparql.ext.datatypes;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.impl.LiteralLabel;

public class RDFDatatypeDelegate
	implements RDFDatatype
{
	protected RDFDatatype delegate;

	public RDFDatatypeDelegate(RDFDatatype delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Object cannonicalise(Object arg0) {
		return delegate.cannonicalise(arg0);
	}

	@Override
	public Object extendedTypeDefinition() {
		return delegate.extendedTypeDefinition();
	}

	@Override
	public int getHashCode(LiteralLabel lit) {
		return delegate.getHashCode(lit);
	}

	@Override
	public Class<?> getJavaClass() {
		return delegate.getJavaClass();
	}

	@Override
	public String getURI() {
		return delegate.getURI();
	}

	@Override
	public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
		return delegate.isEqual(value1, value2);
	}

	@Override
	public boolean isValid(String lexicalForm) {
		return delegate.isValid(lexicalForm);
	}

	@Override
	public boolean isValidLiteral(LiteralLabel lit) {
		return delegate.isValidLiteral(lit);
	}

	@Override
	public boolean isValidValue(Object valueForm) {
		return delegate.isValidValue(valueForm);
	}

	@Override
	public RDFDatatype normalizeSubType(Object value, RDFDatatype dt) {
		return delegate.normalizeSubType(value, dt);
	}

	@Override
	public Object parse(String lexicalForm) throws DatatypeFormatException {
		return delegate.parse(lexicalForm);
	}

	@Override
	public String unparse(Object value) {
		return delegate.unparse(value);
	}

	@Override
	public String toString() {
		return "RDFDatatypeDelegate [delegate=" + delegate + "]";
	}
}
