package org.aksw.jena_sparql_api.io.binseach;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReferenceImpl<T>
	implements Reference<T>
{
	// private static final Logger logger = LoggerFactory.getLogger(ReferenceImpl.class); 
	
	protected T value;
	protected Object comment; // An attribute which can be used for debugging reference chains
	protected ReferenceImpl<T> parent;
	protected AutoCloseable releaseAction;
	protected boolean isReleased = false;
	protected StackTraceElement[] aquisitionStackTrace;

	protected Map<Reference<T>, Object> childRefs = new IdentityHashMap<Reference<T>, Object>();
	
	public ReferenceImpl(ReferenceImpl<T> parent, T value, AutoCloseable releaseAction, Object comment) {
		super();
		
		// logger.debug("Aquired reference " + comment + " from " + parent);
		
		this.parent = parent;
		this.value = value;
		this.releaseAction = releaseAction;
		this.comment = comment;

		boolean traceAquisitions = false;
		if(traceAquisitions) {
			this.aquisitionStackTrace = Thread.currentThread().getStackTrace();
		}
	}
	
	/**
	 * TODO Switch to Java 9 Cleaner once we upgrade
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		if(!isReleased) {
			close();
		}
	}


	public Object getComment() {
		return comment;
	}
	
	@Override
	public T getValue() {
		if(isReleased) {
			throw new RuntimeException("Cannot get value of a released reference");
		}

		return value;
	}

	@Override
	public synchronized Reference<T> aquire(Object comment) {
		// A bit of ugliness to allow the reference to release itself
		@SuppressWarnings("rawtypes")
		Reference[] tmp = new Reference[1];		
		tmp[0] = new ReferenceImpl<T>(this, value, () -> release(tmp[0]), comment);
		
		@SuppressWarnings("unchecked")
		Reference<T> result = (Reference<T>)tmp[0];
		childRefs.put(result, comment);
		return result;
	}
	
//	void release(Reference<T> childRef) {
	protected void release(Object childRef) {
		boolean isContained = childRefs.containsKey(childRef);
		if(!isContained) {
			throw new RuntimeException("An unknown reference requested to release itself. Should not happen");
		} else {
			childRefs.remove(childRef);
		}
		
		checkRelease();
	}

	@Override
	public synchronized void close() {
		if(isReleased) {
			throw new RuntimeException("Reference was already released");
		}
		
		// logger.debug("Released reference " + comment + " to " + parent);

		isReleased = true;
		
		checkRelease();
	}
	
	protected void checkRelease() {
		if(isReleased && childRefs.isEmpty()) {
			if(releaseAction != null) {
				try {
					releaseAction.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
//			if(parent != null) {
//				parent.release(this);
//			}
		}
	}
	
	public static <T> Reference<T> create(T value, AutoCloseable releaseAction, Object comment) {
		return new ReferenceImpl<T>(null, value, releaseAction, comment);
	}

	@Override
	public boolean isReleased() {
		return isReleased;
	}
	
	@Override
	public StackTraceElement[] getAquisitionStackTrace() {
		return aquisitionStackTrace;
	}

	@Override
	public String toString() {
		String result = Stream.concat(
				Stream.of("Reference [" + comment + "] aquired at "),
				aquisitionStackTrace == null
					? Stream.of("unknown location")
					: Arrays.asList(aquisitionStackTrace).stream().map(str -> "  " + Objects.toString(str)))
		.collect(Collectors.joining("\n"));

		return result;
	}
}
