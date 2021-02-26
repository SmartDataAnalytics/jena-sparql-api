
A validator for language tags using the [iana language subtag registry](http://www.iana.org/assignments/language-subtag-registry) as  background knowledge.

Only depends on guava, jena-arq and the dataset.

```java
import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidator;
import org.aksw.jena_sparql_api.langtag.validator.impl.LangTagValidators;


LangTagValidator validator = LangTagValidators.getDefault();

System.out.println(validator.validate("de-at")); // true
System.out.println(validator.validate("english")); // false

```

Uses [Jena's LangTag](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/web/LangTag.html) class to parse language tags into components and validate them against an [RDFized snapshot of the IANA language subtag registry](https://github.com/SmartDataAnalytics/iana-language-subtag-registry-rdf).
