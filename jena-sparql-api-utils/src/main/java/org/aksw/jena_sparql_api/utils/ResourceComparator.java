package org.aksw.jena_sparql_api.utils;

import java.util.Comparator;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Created by Claus Stadler
 * Date: Oct 9, 2010
 * Time: 5:46:56 PM
 */
public class ResourceComparator
    implements Comparator<Resource>
{
    public int compare(Resource a, Resource b) {
        return a.toString().compareTo(b.toString());
    }
}