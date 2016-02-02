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
import org.apache.jena.sparql.path.PathVisitor;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker.Walker;

public class PathWalker
{
    public static void walk(Path path, PathVisitor visitor)
    {
        walk(path, visitor, null, null) ;
    }

    public static void walk(Path path, PathVisitor visitor, PathVisitor beforeVisitor, PathVisitor afterVisitor)
    {
        PathVisitorWalker w = new PathVisitorWalker(visitor, beforeVisitor, afterVisitor) ;
        path.visit(w) ;
    }

    protected static void walk$(Element el, Walker walker)
    {
        el.visit(walker) ;
    }
}
