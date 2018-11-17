package org.aksw.jena_sparql_api.element.transform;

import org.aksw.jena_sparql_api.algebra.transform.TransformDeduplicatePatterns;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;

/**
 * Deduplicate triple / quad patterns;
 * E.g. { ?s ?p ?o . ?s ?p ?o } becomes { ?s ?p ?o }
 * 
 * @author Claus Stadler, Nov 17, 2018
 *
 */
public class ElementTransformDeduplicateBGPs
	extends ElementTransformCopyBase
{
	@Override
	public Element transform(ElementTriplesBlock el) {
		BasicPattern before = el.getPattern();
		BasicPattern after = TransformDeduplicatePatterns.deduplicate(before);
		Element result = after.equals(before) ? el : new ElementTriplesBlock(after);
		return result;
	}

	public Element transform(ElementPathBlock el) {
		PathBlock before = el.getPattern();
		PathBlock after = TransformDeduplicatePatterns.deduplicate(before);

		Element result;
		if(after.equals(before)) {
			result = el;
		} else {			
			ElementPathBlock tmp = new ElementPathBlock();
			tmp.getPattern().addAll(after);
			result = tmp;
		}
		return result;
	}
}
