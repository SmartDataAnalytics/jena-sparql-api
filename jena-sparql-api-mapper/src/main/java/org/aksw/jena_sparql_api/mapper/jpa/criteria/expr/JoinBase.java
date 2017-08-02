package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;

public class JoinBase<Z, X>
    extends FromImpl<Z, X>
    implements Join<Z, X>
{
//    protected final Attribute<? super Z, ?> joinAttribute;
//    protected final JoinType joinType;


    public JoinBase(Path<?> parentPath, String attrName, Class<X> valueType) {
        super(parentPath, attrName, valueType);
    }

    @Override
    public Join<Z, X> on(Expression<Boolean> restriction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Join<Z, X> on(Predicate... restrictions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate getOn() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Attribute<? super Z, ?> getAttribute() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public From<?, Z> getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JoinType getJoinType() {
        // TODO Auto-generated method stub
        return null;
    }
}
