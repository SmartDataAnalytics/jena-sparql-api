## Java&lt;-&gt;RDF Mapper

This module implements a Jena-based object-RDF mapper.
Alibaba, Koma, Empire.

## Usage Example



## Technical Overview
The main classes are:
* EntityManagerJena: A JPA wrapper for the RdfMapperEngine.
* RdfMapperEngine: Core interface defining the most essential methods for mapping between Java objects and RDF data.
  * TODO The Engine aggregates the persistenceContext
  * RdfMapperEngineImpl: Default implementation.
* RdfTypeFactory: A factory for creating RdfTypes from JavaClasses. 
* RdfType: Core interface for mapping between a single Java entity and an RDF graph. An RdfType supports the following things: (1) instantiating a new corresponding Java object, (2) populating it from an RDF graph, (3) serializing an entity as RDF and (4) exposing the SPARQL fragment needed to fetch the data required to populate the RDF object. RdfType implementations exist handling primitive types, classes and RDF collections.
  * RdfClass internally delegates (de)serialization to Populators and PropertyDescriptors.


