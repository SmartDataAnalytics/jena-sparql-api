package org.aksw.jena_sparql_api_sparql_path2;

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

public class PathVisitorRewriteBase
    implements PathVisitorRewrite
{
    protected Path result = null;

    @Override
    public Path getResult() {
        return result;
    }


    @Override
    public void visit(P_Link path) {
        result = path;
    }

    @Override
    public void visit(P_ReverseLink path) {
        result = path;
    }

    @Override
    public void visit(P_NegPropSet path) {
        result = path;
    }

    @Override
    public void visit(P_Inverse path) {
        result = path;
    }

    @Override
    public void visit(P_Mod path) {
        result = path;
    }

    @Override
    public void visit(P_FixedLength path) {
        result = path;
    }

    @Override
    public void visit(P_Distinct path) {
        result = path;
    }

    @Override
    public void visit(P_Multi path) {
        result = path;
    }

    @Override
    public void visit(P_Shortest path) {
        result = path;
    }

    @Override
    public void visit(P_ZeroOrOne path) {
        result = path;
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        result = path;
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        result = path;
    }

    @Override
    public void visit(P_OneOrMore1 path) {
        result = path;
    }

    @Override
    public void visit(P_OneOrMoreN path) {
        result = path;
    }

    @Override
    public void visit(P_Alt path) {
        result = path;
    }

    @Override
    public void visit(P_Seq path) {
        result = path;
    }

}
