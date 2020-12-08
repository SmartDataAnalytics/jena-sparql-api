package org.aksw.jena_sparql_api.mapper;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.function.FunctionEnv;

// FIXME Take LiteralPreference.preferProperties into account
public class AccBestLiteral
    implements Accumulator
{
    protected BestLiteralConfig bestLiteralConfig;
    protected Node bestMatchNode;
    protected int[] bestMatchScore;

    public AccBestLiteral(BestLiteralConfig bestLiteralConfig) {
        this.bestLiteralConfig = bestLiteralConfig;

        this.bestMatchNode = null;

        // Scores for predicate and language; lower means better match
        this.bestMatchScore = new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE};
    }

    @Override
    public void accumulate(Binding binding, FunctionEnv functionEnv) {

        // Evaluate label, property and subject based on the binding

        Node subject = binding.get(bestLiteralConfig.getSubjectVar());
        Node property = binding.get(bestLiteralConfig.getPredicateVar());
        Node label = binding.get(bestLiteralConfig.getObjectVar());

        List<Node> predicates = bestLiteralConfig.getPredicates();
        List<String> langs = bestLiteralConfig.getLangs();

        if(this.bestMatchNode == null) {
            this.bestMatchNode = subject;
        }

        String candidateLang = NodeUtils.getLang(label);

        // Determine the score vector for the property and the language
        int propertyScore = predicates == null ? 0 : predicates.indexOf(property);
        int langScore = langs == null ? 0 : langs.indexOf(candidateLang);

        int[] score = new int[] {propertyScore, langScore};

        boolean allNonNegative = IntStream.of(score).allMatch(item -> item >= 0);

        if (allNonNegative) {
            // Check if the new score is better (less than) than the current best match
            boolean isBetterMatch = AccBestLiteral.<Integer>compareIterators(
                    IntStream.of(score).iterator(),
                    IntStream.of(bestMatchScore).iterator(), (x, y) -> x < y);

            if (isBetterMatch) {
                bestMatchScore = score;
                bestMatchNode = label;
            }
        }
    }

    @Override
    public NodeValue getValue() {
        return bestMatchNode == null  ? null : NodeValue.makeNode(bestMatchNode);
    }


    public static <T> boolean compareIterators(Iterator<T> as, Iterator<T> bs, BiPredicate<T, T> op) {
        boolean result = false;

        while (as.hasNext() && bs.hasNext()) {
            T a = as.next();
            T b = bs.next();

            if (op.test(a, b)) {
                if (op.test(b, a)) {
                    continue;
                }

                result = true;
                break;
            } else { //else if(op(b, a)) {
                if (!op.test(b, a)) {
                    continue;
                }

                result = false;
                break;
            }
        }

        return result;
    };

}
