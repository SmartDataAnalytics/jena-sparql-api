package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.Selection;

public class SelectionJena<X>
    implements Selection<X>
{

    @Override
    public Class<? extends X> getJavaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAlias() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection<X> alias(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCompoundSelection() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        // TODO Auto-generated method stub
        return null;
    }

}
