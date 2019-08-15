## Annotation-based Resource Proxy Generation System

### Introduction
Jena `Resource`s are stateless views over an RDF Graph (EnhGraph to be precise).
This system simplifies the process of creating such custom views by creating a dynamic proxies that implement missing methods based on
the provided annotations. Furthermore, collection views backed by Jena Model's are supported.

### Features

* Method implementation generation works with interfaces, default methods, abstract classes, and classes.
* Support for dynamic RDF collection views* using [TODO link to module]()- i.e. modifications of these views propagate to the resource's backing model.
* All namespaces of the [RDFa initial context](https://www.w3.org/2011/rdfa-context/rdfa-1.1) (and more) pre-registered.


Note: Map view generation is not yet supported, although the Map implementation already exists.


The snippet below summarizes the mapping capabilities by the proxy system.

```java
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

    /** Copies the elements of the provided list into its list view; i.e. invokes getList(); **/
	@Iri("eg:stringList")
	TestResource setList(List<String> strs);
	List<String> getList();

	@Iri("eg:set")
	Set<String> getItems();

	@Iri("eg:set")
	String getRandomItem();

    /** Return a collection view for the given type */
    /** By default, Set view implemnetations are returned for collections */
	@Iri("eg:dynamicSet")
	<T> Collection<T> getDynamicSet(Class<T> clazz);
}
```

### Dynamic collections
Assume the following interface:

```java
interface Person {
  @Iri("foaf:knows")
  Set<Agent> getAcquaintances();

  @Iri("foaf:knows")
  <T extends Resource> Set<T> getDynamicAcquaintances(Class<T> viewClass);
}
```

The problem here is, that we cannot add an arbitrary RDF resource using `person.getAcquaintances().add(someResource)` - because the set is of type
`Person`.
However, using `person.getDynamicAcquaintances(Resource.class).add(someResource)` we can add any instance of RDF resource (but not its superclass RDFNode) as an acquaintance.


**Known issue** Items in dynamic sets are not yet filtered by the @RdfType annotation of the requested class - this means, that


### Proxy vs Traditional Approach Comparision

Here we provide a comparision of the resource implementation approaches based an a simply Person class, whose `firstName` property maps to `foaf:firstName`.

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


###

It is a nice utility that eases creating "POJOs" backed by RDF data. More precise: It allows one to create domain interfaces that (a) extend Jena's 'Resource interface and (b) provide domain specific getters and setters with annotations for RDF mapping.
A proxy utility method (based on cglib) then dynamically creates approprate implementations.
The generated proxy itself just delegates getter/setter invocations to the appropriate methods in jena-sparql-api's `ResourceUtils` (not to be confused with that of Jena). Hence, if one wanted to implement getters/setters manually, the `ResourceUtils` provide useful methods based on Jena's usual Resource class. Thus, the proxy util actually just does the wiring-part which one would otherwise do with repetetive manual efforts.


* **Step 1a: Create an interface extending Resource **

```java
public interface Person
	extends Resource
{
    @Iri("foaf:firstName")
	String getFirstName();
    void setFirstName(String fn);
}
```

* **Step 1b: Optionally, add @RdfType annotation to the class **

If `@RdfType` is used without argument, a default IRI of pattern `"java://" + class.getCanonicalName()` will be created.
In this example, it would evaluate to `java://foo.bar.LsqQuery`.

```java
package foo.bar;

@RdfType("eg:MyType")
public interface LsqQuery
	extends Resource
{
    @Iri("http://lsq.aksw.org/vocab#text")
	String getText();
    void setText(String text);
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
		JenaPluginUtils.scan(MyDomainInterface.class);
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
LsqQuery q = Model.createResource().as(LsqQuery.class);
q.setText("hi");

System.out.println(q.getText());
// Prints "hi"

RDFDataMgr.write(System.out, q.getModel, RDFFormat.NTRIPLES);
// Prints _:b0 <http://lsq.aksw.org/vocab#text> "hi" .

```