package org.aksw.jena_sparql_api.langtag.validator.impl;

import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidator;

public class LangTagValidators {
	private static transient LangTagValidator INSTANCE = null;
	
	/** Return the default validator */
	public static LangTagValidator getDefault() {
		if (INSTANCE == null) {
			synchronized (LangTagValidators.class) {
				if (INSTANCE == null) {
					INSTANCE = LangTagValidatorImpl.createDefault();
				}
			}
		}
		
		return INSTANCE;
	}

}
