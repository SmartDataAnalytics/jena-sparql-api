package org.aksw.isomorphism;

import java.util.Map.Entry;

/***
 * A helper class used by ProblemContainer.
 * The pick method of a that class must return two result values:
 * <ol>
 *   <li>the picked Problem</li>
 *   <li>the remaining workload</li>
 * </ol>
 * This is what this class represents.
 * 
 * Note, that this class inherits from Entry as it is essentially a Tuple2 and
 * can thus be used as such should that ever be needed
 * 
 * @author raven
 *
 * @param <S>
 */
public class ProblemContainerPick<S>
    implements Entry<Problem<S>, ProblemContainerImpl<S>>
{
    protected Problem<S> picked;
    protected ProblemContainerImpl<S> remaining;
    
    public ProblemContainerPick(Problem<S> picked,
            ProblemContainerImpl<S> remaining) {
        super();
        this.picked = picked;
        this.remaining = remaining;
    }
    public Problem<S> getPicked() {
        return picked;
    }
    public ProblemContainerImpl<S> getRemaining() {
        return remaining;
    }
    
    @Override
    public Problem<S> getKey() {
        return picked;
    }
    @Override
    public ProblemContainerImpl<S> getValue() {
        return remaining;
    }
    @Override
    public ProblemContainerImpl<S> setValue(
            ProblemContainerImpl<S> value) {
        throw new UnsupportedOperationException();
    }
    
    
}