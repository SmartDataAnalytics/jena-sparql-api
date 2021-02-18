package org.aksw.jena_sparql_api.langtag.validator;

import org.aksw.jena_sparql_api.langtag.validator.impl.LangTagValidators;
import org.junit.Assert;
import org.junit.Test;

public class LangTagValidatorTests {

	@Test
	public void testKnownValidLangTag() {
		boolean verdict = LangTagValidators.getDefault().validate("de-at");
		Assert.assertTrue(verdict);
	}

	@Test
	public void testInvalidLangTag() {
		boolean verdict = LangTagValidators.getDefault().validate("english");
		Assert.assertFalse(verdict);
	}

}
