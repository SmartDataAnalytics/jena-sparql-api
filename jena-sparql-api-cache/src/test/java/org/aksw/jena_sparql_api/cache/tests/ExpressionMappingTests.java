package org.aksw.jena_sparql_api.cache.tests;

import java.util.Set;

import org.aksw.jena_sparql_api.query_containment.index.ExpressionMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

public class ExpressionMappingTests {
	
	public Node createVariable(String varName) {
		return Var.alloc(varName);
	}
	
	@Test
	public void test() {
        
        BiMap<Node, Node> map = HashBiMap.create();
        map.put(createVariable("v_2"), createVariable("v_2"));
        map.put(NodeFactory.createLiteral("="), NodeFactory.createLiteral("="));
        map.put(NodeFactory.createURI("http://www.example.org/takesCourse"), NodeFactory.createURI("http://www.example.org/takesCourse"));
        map.put(NodeFactory.createBlankNode("_6"), NodeFactory.createBlankNode("_8"));
        map.put(createVariable("x"), createVariable("x"));        
        map.put(NodeFactory.createBlankNode("_4"), NodeFactory.createBlankNode("_5"));
        map.put(NodeFactory.createBlankNode("_5"), NodeFactory.createBlankNode("_6"));
        map.put(NodeFactory.createBlankNode("_2"), NodeFactory.createBlankNode("_3"));
        map.put(NodeFactory.createBlankNode("_3"), NodeFactory.createBlankNode("_4"));
        map.put(NodeFactory.createBlankNode("_0"), NodeFactory.createBlankNode("_0"));
        map.put(createVariable("v_1"), createVariable("v_1"));                
        map.put(NodeFactory.createBlankNode("_1"), NodeFactory.createBlankNode("_1"));
        map.put(NodeFactory.createLiteral("Course10"), NodeFactory.createLiteral("Course10"));
        
        //map = HashBiMap.create();
                
        //, "="="=", http://www.example.org/takesCourse=http://www.example.org/takesCourse, _6=_8, ?x=?x, _4=_5, _5=_6,
        // _2=_3, _3=_4, _0=_0, ?v_1=?v_1, _1=_1, "Course10"="Course10"
        
        Multimap<BiMap<Var, Var>, Set<Set<Expr>>> x = ExpressionMapper.computeResidualExpressions(map,
        		ExprUtils.parse("?v_1 = <http://www.example.org/takesCourse> && ?v_2 = \"Course10\""),
        		ExprUtils.parse("?v_1 = <http://www.example.org/takesCourse> && ?v_2 = \"Course10\" && ?v_2 = \"Course20\""));
        
        System.out.println("Test case: " + x);
	}
}
