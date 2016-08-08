package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;

/**
 * Tree wrapper with bidirectional replacement of certain nodes
 * 
 * @author raven
 *
 * @param <T>
 */
public class TreeReplace<T>
    implements Tree<T>
{
    protected Tree<T> delegate;
    protected BiMap<T, T> delegateToReplacement;
    
    public TreeReplace(Tree<T> delegate, BiMap<T, T> delegateToReplacement) {
        super();
        this.delegate = delegate;
        this.delegateToReplacement = delegateToReplacement;
    }

    @Override
    public T getRoot() {
        T b = delegate.getRoot();
        T result = delegateToReplacement.getOrDefault(b, b);
        return result;
    }

    @Override
    public List<T> getChildren(T b) {
        T a = delegateToReplacement.inverse().getOrDefault(b, b);
        List<T> bs = delegate.getChildren(b);
        List<T> result = bs.stream().map(bx -> delegateToReplacement.getOrDefault(bx, bx)).collect(Collectors.toList());
        
        return result;
    }

    @Override
    public T getParent(T b) {
        T a = delegateToReplacement.inverse().getOrDefault(b, b);
        T result = delegate.getParent(a);
        return result;
    }
}
