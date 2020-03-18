package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.nio.file.Path;

import org.apache.jena.sparql.expr.NodeValue;

@FunctionalInterface
public interface PathFunction {
	NodeValue apply(Path path) throws Exception;
}