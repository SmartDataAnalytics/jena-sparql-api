# RDF Annotations

This module purely features annotations for expressing RDF&lt;-&gt;Java mappings. This module has no dependencies.

These annotations are meant as common building blocks for RDF&lt;-&gt;Java mapping systems.
Hence, the exact meaning of the annotations depends on the type of annotation processor.

## Annotation Processors

The following lists our annotation processor systems.

* [JPA criteria queries over RDF engine prototype](jena-sparql-api-mapper-parent) module.
* [Jena Resource Proxies](jena-sparql-api-mapper-proxy): Extremely powerful yet lightweight framework enabling auto-generation of single and multivalued getter/setter implementations in custom Resource view implementations.


## Annotations Reference

Note: The term property refers to methods and fields.

| Annotation  | Target    | Value  | Description  |
|-------------|-----------|--------|--------------|
| DefaultIri  | Class     | String | Associates a default expression string with the annotated class for the purpose of generating IRIs for instances of the class. It is valid for RDF resources corresponding to this class to have IRIs that do not follow this pattern; e.g. if instances are created from data in a triple store. |
| RdfType     | Class     | -      | Associates an rdf:type IRI with the class. This annotation works in two ways: On the one hand, each resource corresponding to this class is associated with the provided type. On the other hand, all resources of that type define the set of class instances which can be created from the RDF data. |
| Iri         | Property  | String | Assigns an IRI to a property |
| IriNs       | Property  | String | Shorthand to assign an IRI to an attribute: The attribute name is appended to the given namespace |
| MultiValued | Property (of type Collection)  | -      | Controls the RDF mapping strategy of Java collections. MultiValued creates for each item of the collection a triple with the property's corresponding IRI |



## Background
Originally, we created these annotations for our [mapper](jena-sparql-api-mapper-parent) module (with inspiration from [Alibaba](https://bitbucket.org/openrdf/alibaba/src/master/).

This module is a prototype that enables queries over RDF data to be expressed as JPA criteria queries over a Java domain model - i.e. Java classes with RDF mapping annotations. Our engine compiles such JPA queries based on the given mappings to workflows that pose the approprite SPARQL queries and apply post-processing to populate Java objects from the result sets. This effectively hides SPARQL and RDF from the business logic thus promising simpler code.
However, implementing a fully fledged JPA implementation is a lot of effort (for example, implementing the EntityGraph API in order to control eager and lazy fetching of resources), and still, very basic RDF operations, such as adding arbitrary properties to an entity is not directly possible.

Jena already features a system that allows the registration and requesting of custom views for resources in an RDF graph.
For us it turned out that using these annotations for automatically creating implementations for Jena Resources views is much more worthwhile due to the making advanced mapping features available with improved time-efficiency.




