package org.aksw.jena_sparql_api.conjure.entity.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType("eg:OpConvert")
public interface OpConvert
	extends Op1
{
	/**
	 * TODO It should not be necessary having to specify the source content type.
	 * The result of coding operations should be entities with appropriate
	 * content-type and encoding information
	 * 
	 * @return
	 */
	@IriNs("eg")
	String getSourceContentType();
	OpConvert setSourceContentType(String contentType);

	@IriNs("eg")
	String getTargetContentType();
	OpConvert setTargetContentType(String contentType);
	
	OpConvert setSubOp(Op subOp);

	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpConvert create(Op subOp, String sourceContentType, String targetContentType) {
		OpConvert result = subOp.getModel().createResource().as(OpConvert.class)
				.setSubOp(subOp)
				.setSourceContentType(sourceContentType)
				.setTargetContentType(targetContentType);
			return result;
	}
}