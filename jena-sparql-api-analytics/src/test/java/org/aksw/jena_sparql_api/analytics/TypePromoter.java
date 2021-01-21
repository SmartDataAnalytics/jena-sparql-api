package org.aksw.jena_sparql_api.analytics;

import java.util.Map;
import java.util.Set;

interface TypePromoter {
	Map<String, String> promoteTypes(Set<String> datatypeIris);
}