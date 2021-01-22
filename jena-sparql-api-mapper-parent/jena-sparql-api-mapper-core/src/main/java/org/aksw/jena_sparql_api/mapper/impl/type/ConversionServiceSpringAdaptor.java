package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.commons.beans.model.ConversionService;

public class ConversionServiceSpringAdaptor
	implements ConversionService
{
	protected org.springframework.core.convert.ConversionService delegate;
	
	public ConversionServiceSpringAdaptor(org.springframework.core.convert.ConversionService delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public <T> T convert(Object source, Class<T> targetType) {
		return delegate.convert(source, targetType);
	}
	
	@Override
	public <T> boolean canConvert(Class<?> sourceType, Class<T> targetType) {
		return delegate.canConvert(sourceType, targetType);
	}
}
