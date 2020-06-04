package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark an interface's default method as a delegate when toString() is called
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ToString {
}
