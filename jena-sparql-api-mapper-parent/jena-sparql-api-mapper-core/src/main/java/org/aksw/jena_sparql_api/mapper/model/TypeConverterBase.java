package org.aksw.jena_sparql_api.mapper.model;

public abstract class TypeConverterBase
    implements TypeConverter
{
    protected String datatypeURI;
    protected Class<?> javaClass;

    public TypeConverterBase(String datatypeURI, Class<?> javaClass) {
        super();
        this.datatypeURI = datatypeURI;
        this.javaClass = javaClass;
    }

    @Override
    public String getDatatypeURI() {
        return datatypeURI;
    }

    @Override
    public Class<?> getJavaClass() {
        return javaClass;
    }

    @Override
    public String toString() {
        return "TypeConverterImpl [datatypeURI=" + datatypeURI + ", javaClass=" + javaClass + "]";
    }
}
