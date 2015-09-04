package org.aksw.jena_sparql_api.modifier;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Takes all resources with a tmp:locationString ...
 * 
 * TODO Maybe we should use custom sparql functions for enrichment.
 * The main question is, whether and how arrays could be used
 * 
 * 
 * @author raven
 *
 */
public class ModifierModelEnricherOsmId
	implements Modifier<Model> 
{

	@Override
	public void apply(Model item) {
		// TODO Auto-generated method stub
		
	}
}
