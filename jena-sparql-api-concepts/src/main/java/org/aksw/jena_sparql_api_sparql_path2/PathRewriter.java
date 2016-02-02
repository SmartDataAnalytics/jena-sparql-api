package org.aksw.jena_sparql_api_sparql_path2;

import java.util.function.Function;

import org.apache.jena.sparql.path.Path;

public interface PathRewriter
    extends Function<Path, Path>
{
    Path apply(Path path);
}
