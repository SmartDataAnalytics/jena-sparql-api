package org.aksw.jena_sparql_api.rdf.collections;

import com.google.common.base.Converter;

public class ConverterFromNodeConverter<N, T>
	extends Converter<N, T>
{
	protected NodeConverter<N, T> nodeConverter;
	
	public ConverterFromNodeConverter(NodeConverter<N, T> nodeConverter) {
		super();
		this.nodeConverter = nodeConverter;
	}

	@Override
	protected T doForward(N a) {
		T result = nodeConverter.toJava(a);
		return result;
	}

	@Override
	protected N doBackward(T b) {
		N result = nodeConverter.toNode(b);
		return result;
	}	

}
