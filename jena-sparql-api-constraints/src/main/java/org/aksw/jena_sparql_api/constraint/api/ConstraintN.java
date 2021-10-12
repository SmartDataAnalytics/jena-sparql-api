package org.aksw.jena_sparql_api.constraint.api;

import java.util.List;

public abstract class ConstraintN
    // implements Constraint
{
    protected List<Constraint> members;

    public List<Constraint> getMembers() {
        return members;
    }
//    public void stateConstraint(Constraint constraint) {
//        for (Constraint member : members) {
//            member.stateConstraint(constraint);
//        }
//    }
}
