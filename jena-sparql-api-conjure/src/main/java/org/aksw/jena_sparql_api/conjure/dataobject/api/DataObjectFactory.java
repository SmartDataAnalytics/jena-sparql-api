package org.aksw.jena_sparql_api.conjure.dataobject.api;

import org.aksw.jena_sparql_api.conjure.dataref.api.DataRef;

public interface DataObjectFactory {
	DataObject create(DataRef dataRef);
}
