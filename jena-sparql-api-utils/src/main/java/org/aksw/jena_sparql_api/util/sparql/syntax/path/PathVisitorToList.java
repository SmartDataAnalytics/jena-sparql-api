package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;


public class PathVisitorToList
	extends PathVisitorFailByDefault {
	
	protected List<P_Path0> result = new ArrayList<>();
	
	public List<P_Path0> getResult() {
		return result;
	}
	
	@Override
	public void visit(P_Link path) {
		result.add(path);
	}
	
	@Override
	public void visit(P_ReverseLink path) {
		result.add(path);
	}
	
	@Override
	public void visit(P_Seq pathSeq) {
		pathSeq.getLeft().visit(this);
		pathSeq.getRight().visit(this);
	}
	

}
