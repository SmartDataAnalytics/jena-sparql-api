package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.algebra.utils.PatternSummary;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.Utils2;
import org.aksw.jena_sparql_api.concept_cache.core.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.dirty.QfpcMatch;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;

import com.google.common.collect.Sets;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.util.NodeIsomorphismMap;



public class SubsumptionUtils {

    boolean isSubsumed(OpJoin needle, OpJoin haystack) {
        return false;
    }

//    boolean isSubsumed(OpQuadFilterPattern needle, OpQuadFilterPattern haystack) {
//        return false;
//    }

    boolean isSubsumed(OpDistinct needle, OpDistinct haystack) {
        return false;
    }

    boolean isSubsumed(OpProject needle, OpProject haystack) {
        return false;
    }

}


