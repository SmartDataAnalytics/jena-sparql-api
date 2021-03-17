package org.aksw.jena_sparql_api.langtag.validator.api;

/**
 * Interface for language tag validation. May receive additional methods in the future such as for detailed
 * reports of violations.
 * 
 * @author Claus Stadler
 *
 */
public interface LangTagValidator {
	void validate(String langTag) throws LangTagValidationException;
	boolean check(String langTag);
}
