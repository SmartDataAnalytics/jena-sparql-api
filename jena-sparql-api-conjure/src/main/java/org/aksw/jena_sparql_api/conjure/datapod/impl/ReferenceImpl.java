package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceImpl<T>
	implements Reference<T>
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceImpl.class); 
	
	protected T value;
	protected Object comment; // An attribute which can be used for debugging reference chains
	protected ReferenceImpl<T> parent;
	protected AutoCloseable releaseAction;
	protected boolean isReleased = false;
	
	protected Map<Reference<T>, Object> childRefs = new IdentityHashMap<Reference<T>, Object>();
	
	public ReferenceImpl(ReferenceImpl<T> parent, T value, AutoCloseable releaseAction, Object comment) {
		super();
		
		logger.debug("Aquired reference " + comment + " from " + parent);
		
		this.parent = parent;
		this.value = value;
		this.releaseAction = releaseAction;
		this.comment = comment;
	}

	public Object getComment() {
		return comment;
	}
	
	@Override
	public T getValue() {
		return value;
	}

	@Override
	public Reference<T> aquire(Object comment) {
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
	void release(Object childRef) {
		boolean isContained = childRefs.containsKey(childRef);
		if(!isContained) {
			throw new RuntimeException("An unknown reference requested to release itself. Should not happen");
		} else {
			childRefs.remove(childRef);
		}
		
		checkRelease();
	}

	@Override
	public void release() {
		if(isReleased) {
			throw new RuntimeException("Reference was already release");
		}
		
		logger.debug("Released reference " + comment + " to " + parent);

		isReleased = true;
		
		checkRelease();
	}
	
	protected void checkRelease() {
		if(childRefs.isEmpty() && isReleased) {
			try {
				releaseAction.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
//			if(parent != null) {
//				parent.release(this);
//			}
		}
	}
	
	public static <T> Reference<T> create(T value, AutoCloseable releaseAction, Object comment) {
		return new ReferenceImpl<T>(null, value, releaseAction, comment);
	}
}
