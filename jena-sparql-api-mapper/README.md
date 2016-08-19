# The Java-RDF Mapper module

This module enables mapping Java classes to RDF data and vice versa.
Hence, it is similar to projects such as Jena Beans, (todo add list of other projects).

The mapper system features a set of annotations, whereas the default annotation processor first evaluates them as spring expressions and subsequently expands namespace declarations.
Hence, it is possible to e.g. annotate a class with a default expression in order to generate IRIs for its instances.

## A simple example


```java
// A simple class with annotations
@RdfType("ex:Person")
@DefaultIri("ex:#{id}")
class Person {
  @Iri("ex:id")
  private int id;

  @Iri("ex:name")
  private String name;

  // getters and setters omitted for brevity
}

```

## Annotations

| Annotation  | Target    | Value  | Description  |
|-------------|-----------|--------|--------------|
| DefaultIri  | Class     | String | Assigns a default rule to generate IRIs for instances of the class. It is valid for RDF resources corresponding to this class (which may e.g. be stored in a triple store) to have IRIs that do not follow this pattern. |
| RdfType     | Class     | -      | This annotation work in two ways: On the one hand, each resource corresponding to this class is associated with the provided type. On the other hand, all resources of that type define the set of class instances which can be created from the RDF data. |
| Iri         | Property  | String | Assigns an IRI to a property |
| MultiValued | Property (of type Collection)  | -      | Controls the RDF mapping strategy of Java collections. MultiValued creates for each item of the collection a triple with the property's corresponding IRI |


## Usage


```java
SparqlService sparqlService = FluentSparqlService.forModel().create();

Prologue prologue = new Prologue();
// Add prefix declarations to prologue

RdfTypeFactory typeFactory =  RdfTypeFactoryImpl.createDefault(prologue);
RdfMapperEngine engine = new RdfMapperEngineImpl(sparqlService);

Person entity = new Person(1);
engine.merge(entity);


// Write out the triples
Model model = sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct();
model.write(System.out, "TTL");

// Look up a class with a certain ID
Person p = engine.find(Person.class, "ex:0");
System.out.println("Found: " + p);

```

### Constraints

#### Filtering by SPARQL constraints.
It is possible to retrict the set of resources for which to obtain their corresponding Java instances by a SPARQL concept:

```java
Concept concept = Concept.parse("?s ex:id ?id . FILTER(?id < 10)", "s", prologue);
List<Person> people = engine.list(Person.class, concept);

```

#### Filtering using criteria
TODO: Implement parts of JPA's criteria API and create SPARQL concepts from the API calls.



## Registering Java-RDF mappings for primitive types
The mapper uses Jena's TypeMapper to convert between Java objects and RDF terms.
This is not only used for primitive datatypes, such as int, String, ... and (the boxed versions, such as Integer) but also classes
such as Calendar.

```java
TypeMapper tm = TypeMapper.getInstance();
tm.register(new CustomRDFDatatype());
```

By default, the typeMapper is consulted first. If it does not return an RdfType for a given class, the type is considered to be complex, and
another handler is invoked, as described in the next section.


## Registering handlers for complex types
The mapper consults a function for determining a class's corresponding description in terms of an EntityOps instance.
A custom function can be provided for handling custom types.


```
Function<Class<?>, EntityOps> classToOps = (clazz) -> EntityModel.createDefaultModel(clazz);
RdfTypeFactory typeFactory =  RdfTypeFactoryImpl.createDefault(null, classToOps);
```

### Overriding constructors, properties and annotations of entity classes
There exist various tooling for obtaining a model about a bean's features.
Java features the BeanInfo class and the spring framework features the BeanWrapper.
However, in some cases we need to overwrite properties of a bean, such as providing a custom 'default constructor' for a class which does
not define one.

For this reason, we introduce the EntityModel and PropertyModel classes, which as sub-classes of EntityOps and PropertyOps.
Whereas EntityOps provides a newInstance() method, EntityModel allows setting a custom supplier to which the request is delegated to.

```java
EntityModel entityModel = EntityModel.createDefaultModel(Person.class);
// Note: Person does not have a default constructor
entityModel.setNewInstance(() -> new Person(1, "John Doe"));
```

### Mocking
TODO: We could extend the createDefaultModel function to automatically mock constructor arguments - e.g. Mockito


