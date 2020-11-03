package org.aksw.jena_sparql_api.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.core.Var;

public class BestLiteralConfig {

    protected LiteralPreference literalPreference;
    protected Var subjectVar;
    protected Var predicateVar;
    protected Var objectVar;

    public BestLiteralConfig(LiteralPreference literalPreference) {
        this(literalPreference, Vars.s, Vars.p, Vars.o);
    }

    public BestLiteralConfig(
            LiteralPreference literalPreference,
            Var subjectVar,
            Var predicateVar,
            Var objectVar) {
        this.literalPreference = literalPreference;
        this.subjectVar = subjectVar;
        this.predicateVar = predicateVar;
        this.objectVar = objectVar;
    }

    public LiteralPreference getLiteralPreference() {
        return literalPreference;
    }

    public Var getSubjectVar() {
        return subjectVar;
    }

    public Var getPredicateVar() {
        return predicateVar;
    }

    public Var getObjectVar() {
        return objectVar;
    }

    /**
     * Convenience method
     *
     */
    public List<String> getLangs() {
        List<String> result = this.literalPreference.getLangs();
        return result;
    }

    /**
     * Convenience method
     *
     */
    public List<Node> getPredicates() {
        List<Node> result = this.literalPreference.getPredicates();
        return result;
    }

    public static BestLiteralConfig fromProperty(Property property) {
        BestLiteralConfig result = new BestLiteralConfig(new LiteralPreference(
                null, Collections.singletonList(property.asNode()), false));
        return result;
    }

    @Override
    public String toString() {
        String result = Arrays.<Object>asList(
                "BestLabelConfig", getLangs(), getPredicates(),
                getSubjectVar(), getPredicateVar(), getObjectVar())
                .stream().map(Objects::toString).collect(Collectors.joining(", "));
        return result;
    }
}
