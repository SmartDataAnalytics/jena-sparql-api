package org.aksw.jena_sparql_api.rx;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.lang.BlankNodeAllocator;
import org.apache.jena.riot.lang.BlankNodeAllocatorLabel;
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
	/** BNodeGenIdPrefix is a copy of {@link BlankNodeAllocatorLabelEncoded}'s private attribute */
	public static final String BNodeGenIdPrefix         = "genid" ;
	
	private static transient BlankNodeAllocatorAsGivenOrRandom GLOBAL_INSTANCE = null;
	
	public static BlankNodeAllocatorAsGivenOrRandom getGlobalInstance() {
		if (GLOBAL_INSTANCE == null) {
			synchronized (BlankNodeAllocatorAsGivenOrRandom.class) {
				if (GLOBAL_INSTANCE == null) {
					long globalId = Math.abs(new Random().nextLong());
					GLOBAL_INSTANCE = new BlankNodeAllocatorAsGivenOrRandom(globalId);
				}
			}
		}
		
		return GLOBAL_INSTANCE;
	}
	
	
    /** A random per-jvm value used for blank node allocation */ 
    protected final long globalBnodeScope;
    protected final AtomicLong counter;
    
    public BlankNodeAllocatorAsGivenOrRandom(long globalBnodeScope) {
    	this(globalBnodeScope, new AtomicLong());
    }
    
    public BlankNodeAllocatorAsGivenOrRandom(long globalBnodeScope, AtomicLong counter) {
		super();
		this.globalBnodeScope = globalBnodeScope;
		this.counter = counter;
	}

	/** Ignore reset - avoid clashes */
    @Override
    public void reset()         { }

    @Override
    public Node alloc(String label) { return NodeFactory.createBlankNode(label); }
    
    @Override
    public Node create() {
        long x = counter.getAndIncrement();
        String label = BNodeGenIdPrefix + "_" + globalBnodeScope + "_" + counter.getAndIncrement();
        return NodeFactory.createBlankNode(label);
    }
}