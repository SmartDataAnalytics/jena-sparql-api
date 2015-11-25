package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * class Department {
 *     @Iri("ex:employees")
 *     @RdfSeq("-seq")
 *     private Set<Employee> employees;
 * }
 *
 * class Employee {
 *     @MappedBy("employees")
 *     private Department department;
 * }
 *
 * :department123 ex:employees :department123-seq123 .
 * :seq123 rdf:_1 emp456
 *
 * Example:
 *
 * class Department {
 *     @MappedBy("department")
 *     private Set<Employee> employees;
 * }
 *
 * class Employee {
 *     @Iri("ex:employee")
 *     @Inverse
 *     private Department department;
 * }
 *
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface MappedBy {
    String value();
}
