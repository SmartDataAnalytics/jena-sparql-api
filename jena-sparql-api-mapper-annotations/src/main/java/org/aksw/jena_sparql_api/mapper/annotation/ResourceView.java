package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Make a Jena Resource view interface or class discoverable by classpath scanning 
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceView {

}
