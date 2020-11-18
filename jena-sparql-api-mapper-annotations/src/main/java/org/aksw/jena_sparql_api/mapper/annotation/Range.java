package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    // Class<? extends RDFNode would require dependency on jena
    Class<?>[] value();
    boolean useCanAs = true;
}
