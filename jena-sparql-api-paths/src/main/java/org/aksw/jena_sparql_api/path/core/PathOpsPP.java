package org.aksw.jena_sparql_api.path.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOps;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.PathParser;

public class PathOpsPP
    implements PathOps<P_Path0, PathPP>
{
    public static final P_Path0 PARENT = new P_Link(PathOpsNode.PARENT);
    public static final P_Path0 SELF = new P_Link(PathOpsNode.SELF);

    private static PathOpsPP INSTANCE = null;

    public static PathOpsPP get() {
        if (INSTANCE == null) {
            synchronized (PathOpsPP.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PathOpsPP();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public PathPP upcast(Path<P_Path0> path) {
        return (PathPP)path;
    }

    @Override
    public List<P_Path0> getBasePathSegments() {
        return Collections.emptyList();
    }

    @Override
    public Comparator<P_Path0> getComparator() {
        return Comparator.comparing(Object::toString);
    }

    @Override
    public PathPP newPath(boolean isAbsolute, List<P_Path0> segments) {
        return new PathPP(this, isAbsolute, segments);
    }

    @Override
    public PathPP newPath(P_Path0 element) {
        return newPath(false, Collections.singletonList(element));
    }

    @Override
    public P_Path0 getSelfToken() {
        return SELF;
    }

    @Override
    public P_Path0 getParentToken() {
        return PARENT;
    }

    @Override
    public String toString(PathPP path) {
        org.apache.jena.sparql.path.Path tmp = PathUtils.toSparqlPath(path.getSegments());
        String str = tmp.toString(new Prologue(PrefixMapping.Extended));

        String result = (path.isAbsolute() ? "/" : "") + str;

        return result;
    }

    @Override
    public PathPP fromString(String str) {
        str = str.trim();

        boolean isAbsolute = false;

        if (str.startsWith("/")) {
            isAbsolute = true;
            str = str.substring(1);
        }

        org.apache.jena.sparql.path.Path tmp = PathParser.parse(str, PrefixMapping.Extended);
        List<P_Path0> segments = PathUtils.toList(tmp);

        return newPath(isAbsolute, segments);
    }



}