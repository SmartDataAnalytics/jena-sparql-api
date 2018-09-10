package org.aksw.jena_sparql_api.mapper.model;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class RdfList<T>
    extends AbstractList<T>
{
    protected List<Node> itemIds;

    protected boolean isInitialized = false;
    protected Map<Node, T> idToItem;

    protected RdfType itemType;


    protected void refresh() {

        //rdfEntityManager.find(itemType.getTargetClass(), itemIds);

        isInitialized = true;
    }

    @Override
    public T get(int index) {
        Node itemId = itemIds.get(index);

        if(!isInitialized) {
            refresh();
        }

        T result = idToItem.get(itemId);
        return result;
    }

    @Override
    public int size() {
        int result = itemIds.size();
        return result;
    }

}
