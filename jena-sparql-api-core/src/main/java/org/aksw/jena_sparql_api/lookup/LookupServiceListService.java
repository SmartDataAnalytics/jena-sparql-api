package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

public class LookupServiceListService<Concept, Node, V>
	implements LookupService<Node, V>
{
	private ListService<Concept, Node, V> listService;
	
	@Override
	public Map<Node, V> apply(Iterable<Node> arg0) {
		
		//Element e = new ElementFilter(expr)
		throw new RuntimeException("not implemented yet");
	}
}
