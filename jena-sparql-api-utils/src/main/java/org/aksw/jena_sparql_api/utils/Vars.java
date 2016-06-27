package org.aksw.jena_sparql_api.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.core.Var;

public class Vars {
    public static final Var signaturePlaceholder = Var.alloc("_?"); //Var.alloc("__signature_placeholder__");

    public static final Var a = Var.alloc("a");
    public static final Var b = Var.alloc("b");
    public static final Var c = Var.alloc("c");

    public static final Var g = Var.alloc("g");
    public static final Var s = Var.alloc("s");
    public static final Var p = Var.alloc("p");
    public static final Var o = Var.alloc("o");

    public static final Var x = Var.alloc("x");
    public static final Var y = Var.alloc("y");
    public static final Var z = Var.alloc("z");

    public static final Var l = Var.alloc("l");

    public static final Var _ = Var.alloc("_");


    public static final List<Var> spo = Arrays.asList(s, p, o);
    public static final List<Var> gspo = Arrays.asList(g, s, p, o);
}
