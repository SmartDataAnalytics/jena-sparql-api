package org.aksw.jena_sparql_api.langtag.validator;

import org.aksw.jena_sparql_api.langtag.validator.impl.LangTagValidators;
import org.junit.Assert;
import org.junit.Test;

public class LangTagValidatorTests {

	@Test
	public void testKnownValidLangTag() {
		boolean verdict = LangTagValidators.getDefault().check("de-at");
		Assert.assertTrue(verdict);
	}

	@Test
	public void testInvalidLangTag() {
		boolean verdict = LangTagValidators.getDefault().check("english");
		Assert.assertFalse(verdict);
	}

}
