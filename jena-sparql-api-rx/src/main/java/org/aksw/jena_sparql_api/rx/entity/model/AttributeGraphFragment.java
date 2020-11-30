package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rx.EntityGraphFragment;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class AttributeGraphFragment {
    protected List<GraphPartitionJoin> mandatoryJoins;
    protected List<GraphPartitionJoin> optionalJoins;

    public AttributeGraphFragment() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public AttributeGraphFragment(List<GraphPartitionJoin> mandatoryJoins, List<GraphPartitionJoin> optionalJoins) {
        super();
        this.mandatoryJoins = mandatoryJoins;
        this.optionalJoins = optionalJoins;
    }

    public List<GraphPartitionJoin> getMandatoryJoins() {
        return mandatoryJoins;
    }

    public List<GraphPartitionJoin> getOptionalJoins() {
        return optionalJoins;
    }

    public void setMandatoryJoins(List<GraphPartitionJoin> mandatoryJoins) {
        this.mandatoryJoins = mandatoryJoins;
    }

    public void setOptionalJoins(List<GraphPartitionJoin> optionalJoins) {
        this.optionalJoins = optionalJoins;
    }

    /*
     * Convenience functions
     */

    public AttributeGraphFragment addMandatoryJoin(Var entityVar, Query query) {
        getMandatoryJoins()
                .add(new GraphPartitionJoin(EntityGraphFragment.fromQuery(
                        entityVar, query)));
        return this;
    }

    public AttributeGraphFragment addOptionalJoin(Var entityVar, Query query) {
        getOptionalJoins()
            .add(new GraphPartitionJoin(EntityGraphFragment.fromQuery(
                    entityVar, query)));
        return this;
    }

    @Override
    public String toString() {
        String result =
            optionalJoins.stream().map(item -> "OPTIONAL " + item).collect(Collectors.joining("\n")) +
            mandatoryJoins.stream().map(item -> "" + item).collect(Collectors.joining("\n"));

        return result;
    }
}
