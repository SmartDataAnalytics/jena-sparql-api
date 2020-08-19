package org.aksw.jena_sparql_api.mapper.annotation;

public @interface IdPrefix {
    String value() default "";
    String separator() default "-";
}
