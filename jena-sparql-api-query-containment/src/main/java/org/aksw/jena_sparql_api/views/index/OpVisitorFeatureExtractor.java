package org.aksw.jena_sparql_api.views.index;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorByTypeBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpN;

public class OpVisitorFeatureExtractor<T>
    extends OpVisitorByTypeBase
{
    protected Set<T> features;
    protected Function<Op, T> fnToFeature;

    public OpVisitorFeatureExtractor(Function<Op, T> fnToFeature) {
        super();
        this.features = new HashSet<>();
        this.fnToFeature = fnToFeature;
    }

    public Set<T> getFeatures() {
        return features;
    }

    @Override
    protected void visitN(OpN op) {
        T t = fnToFeature.apply(op);
        features.add(t);
    }

    @Override
    protected void visit2(Op2 op) {
        T t = fnToFeature.apply(op);
        features.add(t);
    }

    @Override
    protected void visit1(Op1 op) {
        T t = fnToFeature.apply(op);
        features.add(t);
    }

    @Override
    protected void visit0(Op0 op) {
        T t = fnToFeature.apply(op);
        features.add(t);
    }

    @Override
    protected void visitExt(OpExt op) {
        T t = fnToFeature.apply(op);
        features.add(t);
    }

    @Override
    protected void visitFilter(OpFilter op) {
        T t = fnToFeature.apply(op);
        features.add(t);
    }

    @Override
    protected void visitLeftJoin(OpLeftJoin op) {
        T t = fnToFeature.apply(op);
        features.add(t);
    }


    public static <T> Set<T> getFeatures(Op op, Function<Op, T> fnToFeature) {
        Set<T> result;

        if(op != null)
        {
            OpVisitorFeatureExtractor<T> visitor = new OpVisitorFeatureExtractor<T>(fnToFeature);
            OpWalker.walk(op, visitor);
            result = visitor.getFeatures();
        } else {
            result = Collections.emptySet();
        }
        return result;
    }
}
