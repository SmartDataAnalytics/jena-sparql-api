package org.aksw.jena_sparql_api.beans.json;

import org.aksw.jena_sparql_api.modifier.Modifier;

import com.google.gson.JsonElement;

/**
 * If an item is untyped and has a serviceUri attribute, mark it a sparql endpoint
 *
 * @author raven
 *
 */
public class ModifierJsonAutoTypeSparqlEndpoint
	implements Modifier<JsonElement>
{
	@Override
	public void apply(JsonElement item) {
		if(item.isJsonObject()) {

		}
		// TODO Auto-generated method stub

	}
}
