## Annotation-based Resource Proxy Generation System

### Introduction
Jena `Resource`s are stateless views over an RDF Graph (EnhGraph to be precise).
Our system simplifies the process of creating custom views by creating a dynamic proxies that implement missing methods based on
the provided annotations. Furthermore, collection *views* backed by Jena Model's are supported.

From our experience, the paradigm shift is instead of trying to map RDF into Java classes, it is much easier
to have Java views over RDF - as this way one retains access to all the flexibility of RDF while at the
same time gaining the convenience of a Java domain model. With Java view backed by an RDF resource, one can idiomatically add any triples to that resource (either directly as triples or using the methods of the view). Conversely, adding arbitrary triples to a Java POJO is much more difficult to accomplish - essentially it requires interfacing against some sort of EntityManger.

The ingenious Apache Jena framework already provided most of the conceptual foundations and building blocks - our contribution is to make this sub-system more easily accessible.


### Features

* Method implementation generation works with interfaces, default methods, abstract classes, and classes.
* Support for dynamic RDF collection views using [jena-sparql-api-collections](../jena-sparql-api-collections) module. Modifications of these views propagate to the resource's backing model.
* Polymorphism based on `@RdfType` annotations. Collection views will only expose resources of the requested type or its sub-types.
* All namespaces of the [RDFa initial context](https://www.w3.org/2011/rdfa-context/rdfa-1.1) plus other well known vocabularies, such as GeoSPARQL and Jena's namespaces, pre-registered in the class `DefaultPrefixes` which is loaded by default.


Note: `Map` view generation is not yet supported, although the `MapFromResource` implementations already exists.


The snippet below summarizes the mapping capabilities by the proxy system.

```java
// RdfType: Upon request to view a Resource using this class, append this type to it
// If the argument to @RdfType is omitted, it defaults to "java://" + class.getCanonicalName()
@RdfType("http://my.rdf/Resource")
@ResourceView // Mark as subject to classpath scanning by JenaPluginUtils
public interface ExampleResource
	extends Resource
{
	@Iri("rdfs:label")
	String getString();

	@IriType /** Declare that Strings should be treated as IRIs */
	@Iri("rdfs:seeAlso")
	TestResource setIri(String str);

	@Iri("owl:maxCardinality")
	Integer getInteger();

    // The returned list is a *view* over the RDF model
	@Iri("eg:stringList")
	List<String> getList();

    /** The generated setter invokes getList().addAll(strs) **/
	TestResource setList(List<String> strs);

	@Iri("eg:set")
	Set<String> getItems();

	@Iri("eg:set")
	String getRandomItem();

    /** Return a collection view for the given type */
    /** By default, Set view implementations are returned for collections */
	@Iri("eg:dynamicSet")
	<T> Collection<T> getDynamicSet(Class<T> clazz);
}
```

### Maven

```xml
<dependency>
    <groupId>org.aksw.jena_sparql_api</groupId>
    <artifactId>jena-sparql-api-mapper-proxy</artifactId>
</dependency>
```
Check for the latest version on [Maven Central](https://search.maven.org/search?q=a:jena-sparql-api-mapper-proxy).


### Resource View
The `@ResourceView` annotation accepts a list of classes (`Class<?>`) as arguments which must be equivalent-or-super types of the annotated type.
The generated proxy will be registered only for the provided classes.
Providing no argument is is treated as if the annotated type itself was provided as an argument.

```java
interface MyPlainResource extends Resource {
  String getName();
}

@ResourceView(MyPlainResource.class)
// Also valid (but in this case discouraged as registrations should be frugal):
// @ResourceView(MyPlainResource.class, MyAnnotatedResource.class)
interface MyAnnotatedResource extends MyPlainResource {
  @Override @IriNs("eg") String getName();
}
```

In the code, this can be used as follows:
```java
// Register the implementation from the annotated type:
JenaPluginUtils.registerResourceClass(MyAnnotatedResource.class);

// Now it is possible to request a view using the super types supplied to @ResourceView
MyPlainResource my = ModelFactory.createDefaultModel().createResource(MyPlainResource.class);
```

### Dynamic polymorphic collection views
Dynamic collection view have the signature
```
<T extends BOUND> Set<T> getCollection(Class<T> viewClass);
<T extends BOUND> List<T> getCollection(Class<T> viewClass);
```

`BOUND` is optional and thus defaults to `Object`. If a bound is given, only
resources that can be viewed as BOUND or its sub-classes are exposed in the
requested set.

One key advantage of dynamic collections is extensibility:
Assume a simple model for [DCAT](https://www.w3.org/TR/vocab-dcat/) dataset descriptions:

```
interface DcatDistribution {
  @IriType
  @IriNs("dcat")
  String getDownloadURL();
}

interface DcatDataset {
  @Iri("dcat:distribution")
  Collection<DcatDistribution> getDistributions();
}
```

Now assume you want to reuse this model, however, you would also like to have some additional properties on `DcatDistribution`,
such as whether the distribution has been downloaded to a local file:
```
interface MyCustomDcatDistribution {
  @IriNs("eg")
   String getLocalCacheLocation();
}
```

Clearly, you want to be able to conveniently access the properties of your custom distributions when iterating the distributions of a dataset.
However, the signature `Collection<DcatDistribution> getDistributions()` does not allow for it - it is not even possible to override this method.
Besides, subclassing DcatDataset only shifts the problem - just assume there was a `DcatCatalog` class with a `getDatasets()` method.
So at this point, traditionally you are left with these two choices:

* Subclass all domain classes and add getters / setters for you custom classes - cumbersome. 
* Perform casting in your business logic - ugly.

You may be inclined to think that the issue may be solved if the signature of `getDistributions()` was
```java
interface DcatDataset {
    Collection<? extends DcatDistribution> getDistributions();
    ...
}
This indeed allows subclasses to override the method, however, you can no longer add items to the collection:

DcatDataset dcatDataset = ...
dcatDataset.getDistributions().add(/* no argument is valid here */);

```

With the dynamic collection view, it is possible to set up the view as

```
class DcatDataset {
    @Iri("dcat:distribution")
    <T> Collection<T extends DcatDistribution> getDistributions(Class<T> viewClass);
}
```

Now you can view **any** distribution using your own implementaion - without the cumbersome or ugly drawbacks!
```
DcatDataset dcatDataset = ModelFactory.createDefaultModel().createResource().as(DcatDataset.class);
Collection<MyCustomDistribution> myDistributions = dcatDataset.getDistributions(MyCustomDistribution.class);
```

### Proxy vs Traditional Approach Comparision
The purpose of this section is to give insight into the inner working of the proxy generation.
In principle, based on the method signature, Jena's configuration and information collected during class path scanning, methods are
mapped to appropriate static utility functions in the `ResourceUtils` class. Hence, most proxy methods can be implemented manually using a one-liner.

Here we provide a comparision of the resource implementation approaches based an a simple Person class, whose `firstName` property maps to `foaf:firstName`.

```java
public interface Person extends Resource {
  String getFirstName();
  Person setFirstName(String fn);
}

public class PersonImpl extends ResourceImpl implements Person
{
  	public PersonImpl(Node node, EnhGraph graph) { super(node, graph); }

    public String getFirstName() { return Optional.ofNullable(getProperty(FOAF.firstName)).map(Statement::getString).orElse(null); }
    public Person setFirstName(String fn) { setProperty(FOAF.firstName, fn); return this; }
}
```

Registration of the interface and its implementation to Jena is done using:
```java
BuiltinPersonalities.model.add(Person.class, new SimpleImplementation(PersonImpl::new));
```

This allows for requesting specific views on Resources:
```java
Person person = ModelFactory.createDefaultModel().createResource().as(Person.class).setFirstName("Tim");
```


Obviously, this can get quite repetetive when there are many attributes involved.
Our system allows one to accomplish the same with as little as the followng snippet:
```java
public interface PersonBase extends Resource {
  String getFirstName();
  Person setFirstName(String fn);
}

/** Separate the annotated interface from the non-annotated one for the sake of demonstrating that it can be done */
public interface Person extends PersonBase {
  @IriNs("foaf")
  String getFirstName();
}
```


We provide a util method for generating the proxy implementation from the provided interface and registering it with Jena:
```java
JenaPluginUtils.registerResourceClasses(Person.class);
```


### Step-by-step setup guide


* **Step 1a: Create your annotated interfaces/classes extending Resource, such as **

```java
@RdfType("foaf:Person")
public interface Person
	extends Resource
{
    @Iri("foaf:firstName")
	String getFirstName();
    void setFirstName(String fn);
}
```

* **Step 2a: Use the package scanning utility to automatically register annotated interfaces (or classes) with Jena**.

```java
JenaPluginUtils.scan(LsqQuery.class.getPackage().getName(), BuiltinPersonalities.model);
```

Under the hood, this effectively performs the following registration for each class `cls` having appropriate annotations:
```java
BuiltinPersonalities.model
  .add(cls, new ProxyImplementation(
     MapperProxyUtils.createProxyFactory(cls)));
```


* **Step 2b: Register Proxy Implementations as part of Jena's Life Cycle Management**

```java
package org.myproject.plugin;

public class JenaPluginMyModule
	implements JenaSubsystemLifecycle
{
	@Override
	public void start() {
		init();
	}

	public static void init() {
		JenaPluginUtils.scan(Person.class);
	}
}
```

Add the fully qualified class name to the file

```
echo "package org.myproject.plugin.JenaPluginMyModule" > src/main/resources/META-INF/services/org.apache.jena.sys.JenaSubsystemLifecycle
```
Note: prior to Jena 3.8.0 it used to be `.system.` instead of `.sys.`.


* **Step 3: Use it!**

Jena will out of the box support viewing resources as instances of the given class, and the proxy will take care of the bidirectional mapping between the bean methods and the RDF model.

```
LsqQuery q = Model.createResource().as(Person.class);
q.setFirstName("John");

System.out.println(q.getFirstName());
// Prints "John"

RDFDataMgr.write(System.out, q.getModel, RDFFormat.NTRIPLES);
// Prints _:b0 <http://xmlns.com/foaf/0.1/firstName> "John" .

```


### Technical Stuff / Under the Hood / Behind the Scenesn
The proxy system relies heavily on the following Jena's fundamental classes

* TypeMapper
* Resource
* ExtendedIterator

Based on these building blocks, we added the following wrappers:

* NodeMapper - this is a component that can convert a Jena Node and a Java object. It differs from the type mapper that it allows for mapping a URI to a String or reverse mapping a String to a Node with a pre-configured language literal.
* TypeDecider - an interface which (a) maps a resource to a set of applicable types w.r.t. a base type and (b) can attach the appropriate triples to a given resource to make it recognizable as an instance of a certain type. This approach is generic - i.e. not limited to individual rdf:type triples. 
* RDFNodeMapper - this mapper bundles TypeMapper, TypeDecider and TypeMapper together into a single comprehensive mapper - it can dynamically convert between RDFNodes and Java primitives, as well as Resources and Java RDF views.
* ConverterFrom(RDF)NodeMapper - A wrapper which makes the mappers usable with guava. In particular, our jena-sparql-api-collections module provides mutable collection view classes such as ListFromRDFList and SetFromResource which can be subsequently transformed with a CollectionFromConverter.

