package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.function.Function;

import org.aksw.commons.beans.model.ConversionService;
import org.aksw.commons.beans.model.EntityOps;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.apache.jena.sparql.core.Prologue;
import org.springframework.beans.factory.FactoryBean;

public class RdfTypeFactoryFactoryBean
	implements FactoryBean<RdfTypeFactory>
{
	protected Prologue prologue;
	protected Function<Class<?>, EntityOps> entityOpsFactory;
	protected ConversionService conversionService;
	
	public void setPrologue(Prologue prologue) {
		this.prologue = prologue;
	}

	public void setEntityOpsFactory(Function<Class<?>, EntityOps> entityOpsFactory) {
		this.entityOpsFactory = entityOpsFactory;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Prologue getPrologue() {
		return prologue;
	}

	public Function<Class<?>, EntityOps> getEntityOpsFactory() {
		return entityOpsFactory;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	@Override
	public RdfTypeFactory getObject() throws Exception {		
		RdfTypeFactory result = RdfTypeFactoryImpl.createDefault(prologue, entityOpsFactory, conversionService);
		return result;
	}

	@Override
	public Class<?> getObjectType() {
		return RdfTypeFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
