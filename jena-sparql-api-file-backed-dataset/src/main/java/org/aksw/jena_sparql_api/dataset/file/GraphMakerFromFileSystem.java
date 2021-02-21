package org.aksw.jena_sparql_api.dataset.file;

import java.nio.file.Path;

import org.aksw.commons.io.util.UriToPathUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;

public class GraphMakerFromFileSystem
    implements GraphMaker
{
    protected Path path;

    public GraphMakerFromFileSystem(Path path) {
        super();
        this.path = path;
    }

    @Override
    public Graph create(Node name) {
        String iri = name.getURI();
        Path relPath = UriToPathUtils.resolvePath(iri);
        Path fullPath = path.resolve(relPath).resolve("data.trig");
        DatasetGraph dg;
        try {
            dg = new DatasetGraphWithSync(fullPath, LockPolicy.TRANSACTION);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return dg.getGraph(name);
    //    DatasetGraph dg = DatasetGraphFactory.create();
    //    Graph result = dg.getGraph(name);
    //    return result;
    }
}
