package org.aksw.jena_sparql_api.conjure.dataobject.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;

public interface DataObjectFactory {
	DataObject create(PlainDataRef dataRef);
}
