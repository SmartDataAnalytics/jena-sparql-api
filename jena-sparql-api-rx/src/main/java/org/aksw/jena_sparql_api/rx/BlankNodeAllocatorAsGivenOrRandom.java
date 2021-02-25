package org.aksw.jena_sparql_api.rx;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.lang.BlankNodeAllocator;
import org.apache.jena.riot.lang.BlankNodeAllocatorLabelEncoded;

/**
 * An adaption of {@link BlankNodeAllocatorLabel}/{@link BlankNodeAllocatorLabelEncoded}
 * which passes on labels as given, but allocates fresh ones based on incrementing a value
 * scoped with a random per-jvm value.
 * 
 * This means that any given blank node labels such as _:foo will be retained, but turtle constructs such as
 * :s :p [] will yield unique blank nodes across jvms.
 * 
 * @author Claus Stadler
 *
 */
public class BlankNodeAllocatorAsGivenOrRandom implements BlankNodeAllocator
{
    /** A random per-jvm value used for blank node allocation */ 
    protected static final long globalBnodeScope = Math.abs(new Random().nextLong());
    
    protected AtomicLong counter = new AtomicLong(0);
    
    @Override
    public void reset()         { counter = new AtomicLong(0); }

    @Override
    public Node alloc(String label) { return NodeFactory.createBlankNode(label); }
    
    @Override
    public Node create() {
        long x = counter.getAndIncrement();
        String label = SysRIOT.BNodeGenIdPrefix + "_" + globalBnodeScope + "_" + counter.getAndIncrement();
        return NodeFactory.createBlankNode(label);
    }
}