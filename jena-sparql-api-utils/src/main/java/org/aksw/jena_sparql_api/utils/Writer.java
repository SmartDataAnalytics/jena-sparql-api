package org.aksw.jena_sparql_api.utils;

import java.io.OutputStream;

public interface Writer<T> {
	void write(OutputStream out, T obj);
}