package org.aksw.jena_sparql_api.constraint.api;

import java.util.Iterator;

/**
 * If any member becomes inconsistent so becomes the whole conjunction
 *
 * @author raven
 *
 */
public class C_LogicalAnd
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

    public boolean isContradiction() {
        return members.isEmpty();
    }
}
