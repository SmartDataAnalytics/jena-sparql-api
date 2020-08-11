package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.sparql.syntax.Element;

/**
 * Combines a SPARQL element with a row wise mapping to Java objects
 * 
 * Note: You can use ElementSubQuery in cases where you need to map query output
 * 
 * @author raven
 *
 * @param <T>
 */
public class MappedElement<T> {
    private Element element;
    private BindingMapper<T> bindingMapper;
    
    public MappedElement(Element element, BindingMapper<T> bindingMapper) {
        this.element = element;
        this.bindingMapper = bindingMapper;
    }

    public Element getElement() {
        return element;
    }

    public BindingMapper<T> getBindingMapper() {
        return bindingMapper;
    }
}
