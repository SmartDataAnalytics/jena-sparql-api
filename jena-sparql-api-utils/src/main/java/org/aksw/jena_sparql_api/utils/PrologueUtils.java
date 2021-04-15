package org.aksw.jena_sparql_api.utils;

import java.lang.reflect.Field;

import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.sparql.core.Prologue;

public class PrologueUtils {
	/** The missing counterpart to Prologue.getResolver in Jena 4.0.0 */
	public static Prologue setResolver(Prologue prologue, IRIxResolver resolver) {
		Field field;
		try {
			field = Prologue.class.getDeclaredField("resolver");
			field.setAccessible(true);
	        field.set(prologue, resolver);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return prologue;
	}
}
