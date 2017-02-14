package org.aksw.jena_sparql_api.sparql.ext.datatypes;

import java.util.Calendar;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;

public class RDFDatatypeCalendar
	extends RDFDatatypeDelegate
{
	protected Class<?> clazz;

	public RDFDatatypeCalendar() {
		super(new XSDDateTimeType("dateTime"));
		this.clazz = Calendar.class;
	}

	@Override
	public Class<?> getJavaClass() {
		return clazz;
	}

	public String unparse(Object value) {
		Calendar cal = (Calendar)value;
		XSDDateTime tmp = new XSDDateTime(cal);
		String result = super.unparse(tmp);
		return result;
	}

	@Override
	public Object parse(String lexicalForm) {
		Object tmp = super.parse(lexicalForm);
		XSDDateTime xsd = (XSDDateTime)tmp;
		Calendar result = xsd.asCalendar();
		return result;
	}
}
