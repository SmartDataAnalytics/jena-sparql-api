package org.aksw.jena_sparql_api.dereference;

import org.aksw.commons.collections.IClosable;

import org.apache.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/30/11
 *         Time: 4:23 PM
 */
public interface Dereferencer
    extends IClosable
{
    Model dereference(String url);
}
