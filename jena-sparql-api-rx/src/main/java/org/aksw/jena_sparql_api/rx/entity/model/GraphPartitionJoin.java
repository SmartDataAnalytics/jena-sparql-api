package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.List;

import org.aksw.jena_sparql_api.rx.EntityGraphFragment;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;


public class GraphPartitionJoin
{
    protected EntityGraphFragment entityGraphFragment;

    protected List<Var> parentJoinVars; // if null the join will occur on the parent's partition vars
    protected String lazyFetchGroupName;

    protected List<GraphPartitionJoin> subJoins;

    public GraphPartitionJoin(EntityGraphFragment entityGraphFragment) {
        this(entityGraphFragment, null, null, null);
    }

    public GraphPartitionJoin(EntityGraphFragment entityGraphFragment,
            List<Var> parentJoinVars, String lazyFetchGroup, List< GraphPartitionJoin> subJoins) {
        super();
        this.entityGraphFragment = entityGraphFragment;
        this.parentJoinVars = parentJoinVars;
        this.lazyFetchGroupName = lazyFetchGroup;
        this.subJoins = subJoins;
//        this.isOptional = isOptional;
    }

//    public static GraphPartitionJoin create(Query query, List<Var> partitionVars, Node entityNode) {
//        return new GraphPartitionJoin(
//                new EntityGraphFragment(
//                        partitionVars,
//                        new EntityTemplateImpl(Collections.singletonList(entityNode), query.getConstructTemplate()),
//                        query.getQueryPattern()),
//                null);
//    }

    public List<Var> getParentJoinVars() {
        return parentJoinVars;
    }

    public EntityGraphFragment getEntityGraphFragment() {
        return entityGraphFragment;
    }

    public GraphPartitionJoin applyNodeTransform(NodeTransform nodeTransform) {

        // FIXME We need to handle renaming of parent join vars


//    	List<GraphPartitionJoin> newJoins = new ArrayList<>();
//        for (GraphPartitionJoin subJoin : subJoins) {
//            List<Var> vars = subJoin.getParentJoinVars();
//            List<Var> newVars = NodeTransformLib.transformVars(nodeTransform, vars);
//        }


        GraphPartitionJoin result = new GraphPartitionJoin(
//                isOptional,
                entityGraphFragment.applyNodeTransform(nodeTransform),
                parentJoinVars,
                lazyFetchGroupName,
                subJoins);

        return result;
    }

    public String getLazyFetchGroupName() {
        return lazyFetchGroupName;
    }


    public void setLazyFetchGroupName(String name) {
        this.lazyFetchGroupName = name;
    }

    public List<GraphPartitionJoin> getSubJoins() {
        return subJoins;
    }

    @Override
    public String toString() {
        return "GraphPartitionJoin [entityGraphFragment=" + entityGraphFragment + ", parentJoinVars=" + parentJoinVars
                + ", lazyFetchGroupName=" + lazyFetchGroupName + ", subJoins=" + subJoins + "]";
    }

//    public boolean isOptional() {
//        return isOptional;
//    }
//
//    public void setOptional(boolean isOptional) {
//        this.isOptional = isOptional;
//    }
}

