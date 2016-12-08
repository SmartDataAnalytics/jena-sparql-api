package org.aksw.jena_sparql_api.sparql.ext.datatypes;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;

public class RDFDatatypeDate
    extends RDFDatatypeDelegate
{
    protected Class<?> clazz;

    public RDFDatatypeDate() {
        super(new XSDDateTimeType("dateTime"));
        this.clazz = Date.class;
    }

    @Override
    public Class<?> getJavaClass() {
        return clazz;
    }

    public String unparse(Object value) {
        Date date = (Date) value;
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        XSDDateTime tmp = new XSDDateTime(cal);
        String result = super.unparse(tmp);
        return result;
    }

    @Override
    public Object parse(String lexicalForm) {
        Object tmp = super.parse(lexicalForm);
        XSDDateTime xsd = (XSDDateTime) tmp;
        Calendar cal = xsd.asCalendar();
        Date result = cal.getTime();
        return result;
    }
}
