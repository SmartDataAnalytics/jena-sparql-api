package org.aksw.jena_sparql_api.constraint.api;

import java.util.Iterator;

public class C_LogicalOr
    extends ConstraintN
{
    public void stateConstraint(Constraint constraint) {
        for (Iterator<Constraint> it = members.iterator(); it.hasNext();) {
            Constraint member = it.next();

            if (member.stateConstraint(constraint)) {
                it.remove();
            }
        }
    }

    public boolean isInconsistent() {
        return members.isEmpty();
    }
}
