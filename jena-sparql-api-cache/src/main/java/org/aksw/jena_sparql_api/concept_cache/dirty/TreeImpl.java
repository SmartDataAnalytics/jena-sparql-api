package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concept_cache.op.TreeUtils;

public class TreeImpl<T>
    implements Tree<T>
{
    protected T root;
    protected Function<T, List<T>> parentToChild;
    protected Map<T, T> childToParent;

    public TreeImpl(T root, Function<T, List<T>> parentToChildren, Map<T, T> childToParent) {
        super();
        this.root = root;
        this.parentToChild = parentToChildren;
        this.childToParent = childToParent;
    }

    @Override
    public T getRoot() {
        return root;
    }

    @Override
    public List<T> getChildren(T node) {
        List<T> result = parentToChild.apply(node);
        return result;
    }

    @Override
    public T getParent(T node) {
        T result = childToParent.get(node);
        return result;
    }

    public static <T> TreeImpl<T> create(T root, Function<T, List<T>> parentToChildren) {
        Map<T, T> childToParent = TreeUtils.parentMap(root, parentToChildren);

        TreeImpl<T> result = new TreeImpl<>(root, parentToChildren, childToParent);
        return result;
    }
}
