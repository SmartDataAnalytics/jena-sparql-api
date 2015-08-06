package org.aksw.jena_sparql_api.batch.processor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Combines a resource with two models, one for data and the other for meta data.
 * 
 * This class is used to represent relevant information about a single resource, and thus it is assumed that this information
 * fits into memory - similar to a row in a table, i.e. a resource's properties correspond to (a possibly nested) set of columns.
 * 
 * The resource must be a resource present in the dataModel.
 * The dataModel is assumed to capture (possibly indirectly) related information about the resource.
 * The metaModel is used to tag resources in the dataModel for further processing. 
 * 
 * Conceptually, the idea is, that the meta model holds transient data needed in the processing pipeline, such as
 * for tagging resources as feature / geometry.
 * The dataModel contains the final state of referenced resources (TODO under a given context)
 * and this data will be used to update the resource.
 * 
 * 
 * TODO How to provide the context for resources? SHACL? SHEX? roll our own solution?
 * 
 * 
 * @author raven
 *
 */
public class ResourceModel
{
    /*
     * Alternative:
     * Node node;
     * SparqlService dataService;
     * Sparqlservice metaService; 
     * 
     */
    
    private Resource resource; // A resource of the dataModel
    private Model dataModel;

    private Model metaModel;

    
    public Resource getResource() {
        return resource;
    }
    
    public Model getDataModel() {
        return dataModel;
    }
    
    public Model getMetaModel() {
        return metaModel;
    }
}
