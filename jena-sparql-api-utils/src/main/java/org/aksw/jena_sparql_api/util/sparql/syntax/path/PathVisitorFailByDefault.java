package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;

public class PathVisitorFailByDefault
	implements PathVisitor {
	@Override public void visit(P_Link pathNode) { unhandledPath(pathNode); }
	@Override public void visit(P_ReverseLink pathNode) { unhandledPath(pathNode); }
	@Override public void visit(P_NegPropSet pathNotOneOf) { unhandledPath(pathNotOneOf); }
	@Override public void visit(P_Inverse inversePath) { unhandledPath(inversePath); }
	@Override public void visit(P_Mod pathMod) {  unhandledPath(pathMod); }
	@Override public void visit(P_FixedLength pFixedLength) { unhandledPath(pFixedLength); }
	@Override public void visit(P_Distinct pathDistinct) { unhandledPath(pathDistinct); }
	@Override public void visit(P_Multi pathMulti) { unhandledPath(pathMulti); }
	@Override public void visit(P_Shortest pathShortest) { unhandledPath(pathShortest); }
	@Override public void visit(P_ZeroOrOne path) { unhandledPath(path); }
	@Override public void visit(P_ZeroOrMore1 path) { unhandledPath(path); }
	@Override public void visit(P_ZeroOrMoreN path) { unhandledPath(path); }
	@Override public void visit(P_OneOrMore1 path) { unhandledPath(path); }
	@Override public void visit(P_OneOrMoreN path) { unhandledPath(path); }
	@Override public void visit(P_Alt pathAlt) { unhandledPath(pathAlt); }
	@Override public void visit(P_Seq pathSeq) { unhandledPath(pathSeq); }

	public void unhandledPath(Path path) {
		throw new UnsupportedOperationException("Unspported argument: " + path + " " + (path == null ? "" : "" + path.getClass()));
	}
}