package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rx.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.EntityGraphFragment;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;


/** Basic implementation of {@link EntityQueryBasic} */
public class EntityQueryImpl
{
    protected EntityBaseQuery baseQuery;
    protected AttributeGraphFragment attributePart;

    public EntityQueryImpl() {
        this(null, new AttributeGraphFragment());
    }

    public EntityQueryImpl(EntityBaseQuery baseQuery, AttributeGraphFragment attributePart) {
        super();
        this.baseQuery = baseQuery;
        this.attributePart = attributePart;
    }

    public EntityBaseQuery getBaseQuery() {
        return baseQuery;
    }

    public void setBaseQuery(EntityBaseQuery baseQuery) {
        this.baseQuery = baseQuery;
    }

    public AttributeGraphFragment getAttributePart() {
        return attributePart;
    }

    public void setAttributePart(AttributeGraphFragment attributePart) {
        this.attributePart = attributePart;
    }

    public List<GraphPartitionJoin> getMandatoryJoins() {
        return attributePart.getMandatoryJoins();
    }

    public List<GraphPartitionJoin> getOptionalJoins() {
        return attributePart.getOptionalJoins();
    }

    public void setOptionalJoins(List<GraphPartitionJoin> optionalJoins) {
        this.attributePart.setOptionalJoins(optionalJoins);
    }

    public static EntityQueryImpl createEntityQuery(Var entityVar, Query standardQuery) {
        EntityBaseQuery ebq = new EntityBaseQuery(
                Collections.singletonList(entityVar),
                new EntityTemplateImpl(), standardQuery);
        EntityQueryImpl result = new EntityQueryImpl();
        result.setBaseQuery(ebq);

        return result;
    }

    public static Query createStandardQuery(Var entityVar, Node node) {
        Query result = createStandardQuery(entityVar, Collections.singleton(node));
        return result;
    }

    public static Query createStandardQuery(Var entityVar, Collection<Node> nodes) {
        Query result = createStandardQuery(entityVar, new ElementData(
                Collections.singletonList(entityVar),
                nodes.stream().map(n -> BindingFactory.binding(Vars.s, n)).collect(Collectors.toList())));

        return result;
    }

    public static Query createStandardQuery(Var entityVar, Element element) {
        Query query = new Query();
        query.setQuerySelectType();
        query.getProject().add(entityVar);
        query.setQueryPattern(element);

        return query;
    }


    /**
     * A convenience function to create an entity query for a specific entity (denoted by the node)
     *
     * @param entityGraphFragment
     * @param node
     * @return
     */
    public static EntityQueryImpl createEntityQuery(EntityGraphFragment entityGraphFragment, Node node) {
        Var entityVar = Vars.s;

        Query standardQuery = createStandardQuery(entityVar, node);
        EntityQueryImpl result = EntityQueryImpl.createEntityQuery(entityVar, standardQuery);

        result.getMandatoryJoins().add(new GraphPartitionJoin(entityGraphFragment));

        return result;
    }


    @Override
    public String toString() {
        return attributePart + " " + baseQuery;
    }
}
