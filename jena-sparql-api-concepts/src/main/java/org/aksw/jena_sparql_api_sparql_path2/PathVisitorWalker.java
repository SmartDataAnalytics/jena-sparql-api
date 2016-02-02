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

public class PathVisitorWalker implements PathVisitor
{
    protected final PathVisitor proc ;
    protected final PathVisitor beforeVisitor ;
    protected final PathVisitor afterVisitor ;

    protected PathVisitorWalker(PathVisitor visitor, PathVisitor beforeVisitor, PathVisitor afterVisitor)
    {
        proc = visitor ;
        this.beforeVisitor= beforeVisitor ;
        this.afterVisitor = afterVisitor ;
    }

    private void before(Path path)
    {
        if ( beforeVisitor != null )
            path.visit(beforeVisitor) ;
    }

    private void after(Path path)
    {
        if ( afterVisitor != null )
            path.visit(afterVisitor) ;
    }

    @Override
    public void visit(P_Link path) {
        before(path);
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_ReverseLink path) {
        before(path);
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_NegPropSet path) {
        before(path);
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_Inverse path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_Mod path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_FixedLength path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_Distinct path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_Multi path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_Shortest path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_ZeroOrOne path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_OneOrMore1 path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_OneOrMoreN path) {
        before(path);
        if (path.getSubPath() != null) {
            path.getSubPath().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_Alt path) {
        before(path);
        if (path.getLeft() != null) {
            path.getLeft().visit(this);
        }
        if (path.getRight() != null) {
            path.getRight().visit(this);
        }
        proc.visit(path);
        after(path);
    }

    @Override
    public void visit(P_Seq path) {
        before(path);
        if (path.getLeft() != null) {
            path.getLeft().visit(this);
        }
        if (path.getRight() != null) {
            path.getRight().visit(this);
        }
        proc.visit(path);
        after(path);
    }
}