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

/**
 * rewrite(E(a1, ..., an)) -> X(rewrite(a1), ..., rewrite(an))
 *
 * @author raven
 *
 */
public class PathVisitorTopDown
    implements PathVisitorRewrite
{
    protected PathRewriter immediatePathRewriter;
    protected Path result = null;

    public PathVisitorTopDown(PathRewriter immediatePathRewriter) {
        this.immediatePathRewriter = immediatePathRewriter;
    }

    @Override
    public Path getResult() {
        return result;
    }

    public Path apply(Path path) {
        Path result = apply(path, immediatePathRewriter);
        return result;
    }

    /**
     * Convenience function that wraps a visitor that computes a result as a rewriter
     * @param path
     * @param visitor
     * @return
     */
    public static Path apply(Path path, PathVisitorRewrite visitor) {
        PathRewriter rewriter = new PathRewriterVisitor(visitor);
        Path result = apply(path, rewriter);
        return result;
    }

    public static Path apply(Path path, PathRewriter immediatePathRewriter) {
        Path newPath = immediatePathRewriter.apply(path);
        PathVisitorTopDown descend = new PathVisitorTopDown(immediatePathRewriter);
        newPath.visit(descend);
        Path result = descend.getResult();
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
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_Inverse(newSubPath)
                ;
    }

    @Override
    public void visit(P_Mod path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_Mod(newSubPath, path.getMin(), path.getMax())
                ;
    }

    @Override
    public void visit(P_FixedLength path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_FixedLength(newSubPath, path.getCount())
                ;
    }

    @Override
    public void visit(P_Distinct path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_Distinct(newSubPath)
                ;
    }

    @Override
    public void visit(P_Multi path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_Multi(newSubPath)
                ;
    }

    @Override
    public void visit(P_Shortest path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_Shortest(newSubPath)
                ;
    }

    @Override
    public void visit(P_ZeroOrOne path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_ZeroOrOne(newSubPath)
                ;
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_ZeroOrMore1(newSubPath)
                ;
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_ZeroOrMoreN(newSubPath)
                ;
    }

    @Override
    public void visit(P_OneOrMore1 path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_OneOrMore1(newSubPath)
                ;
    }

    @Override
    public void visit(P_OneOrMoreN path) {
        Path newSubPath = apply(path.getSubPath());

        result = path.getSubPath() == newSubPath
                ? path
                : new P_OneOrMoreN(newSubPath)
                ;
    }

    @Override
    public void visit(P_Alt path) {
        Path newLeft = apply(path.getLeft());
        Path newRight = apply(path.getRight());

        result = path.getLeft() == newLeft && path.getRight() == newRight
                ? path
                : new P_Alt(newLeft, newRight)
                ;
    }

    @Override
    public void visit(P_Seq path) {
        Path newLeft = apply(path.getLeft());
        Path newRight = apply(path.getRight());

        result = path.getLeft() == newLeft && path.getRight() == newRight
                ? path
                : new P_Seq(newLeft, newRight)
                ;
    }
}
