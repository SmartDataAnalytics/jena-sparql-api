package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.IdentityHashMap;
import java.util.Map;

public class ReferenceImpl<T>
	implements Reference<T>
{
	protected T value;
	protected Object comment; // An attribute which can be used for debugging reference chains
	protected ReferenceImpl<T> parent;
	protected AutoCloseable releaseAction;
	protected boolean isReleased = false;
	
	protected Map<Reference<T>, Object> childRefs = new IdentityHashMap<Reference<T>, Object>();
	
	public ReferenceImpl(ReferenceImpl<T> parent, T value, AutoCloseable releaseAction, Object comment) {
		super();
		this.parent = parent;
		this.value = value;
		this.releaseAction = releaseAction;
		this.comment = comment;
	}

	
	@Override
	public T getValue() {
		return value;
	}

	@Override
	public Reference<T> aquire(Object comment) {
		Reference<T> result = new ReferenceImpl<T>(this, value, () -> release(this), comment);
		childRefs.put(result, comment);
		return result;
	}
	
	void release(Reference<T> childRef) {
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
		
		isReleased = false;
		
		checkRelease();
	}
	
	protected void checkRelease() {
		if(childRefs.isEmpty() && isReleased) {
			try {
				releaseAction.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if(parent != null) {
				parent.release(this);
			}
		}
	}
	
	public static <T> Reference<T> create(T value, AutoCloseable releaseAction, Object comment) {
		return new ReferenceImpl<T>(null, value, releaseAction, comment);
	}
}
