package org.aksw.jena_sparql_api.sparql.ext.csv;

import joptsimple.ValueConverter;

public class ValueConverterCharacter
	implements ValueConverter<Character>
{

	@Override
	public Character convert(String value) {
		Character result;
		if(value == null || value.isEmpty()) {
			//throw new RuntimeException("Cannot convert null or empty string to character");
			result = null;
		} else if(value.length() > 1) {
				throw new RuntimeException("Character conversion does not support strings with more than 1 character");	
		} else {
			result = value.charAt(0);
		}
		
		return result;
	}

	@Override
	public Class<? extends Character> valueType() {
		return Character.class;
	}

	@Override
	public String valuePattern() {
		return null;
	}

}
