package org.aksw.jena_sparql_api.cache.staging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.commons.collections.IClosable;

@Deprecated // Does not seem to be used anymore
public class CloseActionCollection
	implements IClosable
{
	private Collection<IClosable> closables;
	
	public CloseActionCollection() {
		closables = new ArrayList<IClosable>();
	}
	
	public CloseActionCollection(Collection<IClosable> closables) {
		this.closables = closables; 
	}
	
	public Collection<IClosable> getClosables() {
		return this.closables;
	}

	@Override
	public void close() {
		List<Exception> exceptions = null;
		for(IClosable closable: closables) {
			try {
				closable.close();
			} catch(Exception e) {
				if(exceptions == null) {
					exceptions = new ArrayList<Exception>();
					exceptions.add(e);
				}
			}
		}
		
		if(exceptions != null) {
			throw new RuntimeException(exceptions.size() + " exceptions thrown." + exceptions);
		}		
	}
}