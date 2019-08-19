package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.base.Converter;

/**
 * 
 * @author raven
 *
 * @param <R> The RDFNode type
 * @param <J> The Java type
 */
public class ConverterFromNodeMapperAndModel<R extends RDFNode, J>
	extends Converter<R, J>
{
	protected Model model;
	protected Converter<Node, J> converter;
	protected Class<R> rdfViewClass;
	
	public ConverterFromNodeMapperAndModel(Model model, Class<R> rdfViewClass, Converter<Node, J> converter) {
		super();
		this.model = model;
		this.converter = converter;
		this.rdfViewClass = rdfViewClass;
	}

	@Override
	protected J doForward(R a) {
		Node node = a.asNode();
		J result = converter.convert(node); //nodeMapper.toJava(node);
		return result;
	}

	@Override
	protected R doBackward(J b) {
		Node node = converter.reverse().convert(b);//nodeMapper.toNode(b);
		RDFNode tmp = model.asRDFNode(node);
		R result = tmp.as(rdfViewClass);
		return result;
	}	
}
