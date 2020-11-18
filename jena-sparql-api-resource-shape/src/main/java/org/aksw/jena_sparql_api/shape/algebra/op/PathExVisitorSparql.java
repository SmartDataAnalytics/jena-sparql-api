package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jena_sparql_api.shape.syntax.P_Element;
import org.aksw.jena_sparql_api.shape.syntax.P_Relation;
import org.aksw.jena_sparql_api.shape.syntax.P_Service;
import org.aksw.jena_sparql_api.shape.syntax.P_Var;
import org.aksw.jena_sparql_api.shape.syntax.PathExVisitor;
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

public class PathExVisitorSparql
    implements PathExVisitor
{

    @Override
    public void visit(P_Link pathNode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_ReverseLink pathNode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_NegPropSet pathNotOneOf) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Inverse inversePath) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Mod pathMod) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_FixedLength pFixedLength) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Distinct pathDistinct) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Multi pathMulti) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Shortest pathShortest) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_ZeroOrOne path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_OneOrMore1 path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_OneOrMoreN path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Alt pathAlt) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Seq pathSeq) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Relation path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Service path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Var path) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(P_Element path) {
        // TODO Auto-generated method stub

    }

}
