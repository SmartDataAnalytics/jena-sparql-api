package org.aksw.jena_sparql_api.dataset.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.SymLinkUtils;
import org.aksw.commons.io.util.UriToPathUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.apache.jena.atlas.iterator.IteratorConcat;
import org.apache.jena.ext.com.google.common.io.MoreFiles;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.util.NodeUtils;

public class DatasetGraphIndexerFromFileSystem
    implements DatasetGraphIndexPlugin
{
    protected Path basePath;
//    protected Path propertyFolder;
    protected Node propertyNode;
    protected Function<? super Node, Path> objectToPath;
    protected Function<String, Path> uriToPath;
    
    
    // We need that graph in order to re-use its mapping
    // from (subject) iris to paths
    protected DatasetGraphFromFileSystem syncedGraph;

    public DatasetGraphIndexerFromFileSystem(
            DatasetGraphFromFileSystem syncedGraph,
            Node propertyNode,
            Path basePath,
            Function<Node, Path> objectToPath) {
        super();
        this.basePath = basePath;
        this.propertyNode = propertyNode;
        this.syncedGraph = syncedGraph;
        this.objectToPath = objectToPath;
    }

    public static Path uriNodeToPath(Node node) {
        Path result = node.isURI()
                ? UriToPathUtils.resolvePath(node.getURI())
                : null;

        return result;
    }


    public static Path mavenStringToToPath(Node node) {
        String str = node.isURI() ? node.getURI() : NodeUtils.stringLiteral(node);

        // Remove trailing slashes and
        // FIXME Replace '..' only if it would be interpreted as moving up the directory
        Path result = null;
        if (str != null) {
            String[] parts = str.replace(':', '/').replaceAll("^/*", "").split("/");
            String tmp = Arrays.asList(parts).stream()
                .filter(part -> !part.equals(".."))
                .map(StringUtils::urlEncode)
                .collect(Collectors.joining("/"));

            result = Paths.get(tmp);
        }

        return result;
    }


    @Override
    public void add(Node g, Node s, Node p, Node o) {
//        Node g = quad.getGraph();
//        Node s = quad.getSubject();
//        Node p = quad.getPredicate();
//        Node o = quad.getObject();
        if (evaluateFind(s, p, o) != null) {
            Path idxRelPath = objectToPath.apply(o);


//            Path idxRelPath = UriToPathUtils.resolvePath(oRelPath);
            Path idxFullPath = basePath.resolve(idxRelPath);

            String tgtIri = g.getURI();
            Path tgtBasePath = syncedGraph.getBasePath();
            Path tgtRelPath = syncedGraph.getRelPathForIri(tgtIri);
            String tgtFileName = syncedGraph.getFilename();
            Path symLinkTgtAbsFile = tgtBasePath.resolve(tgtRelPath).resolve(tgtFileName);
//            Path symLinkTgtRelFile = idxFullPath.relativize(symLinkTgtAbsFile);

            Path file = Paths.get(tgtFileName);
            String prefix = MoreFiles.getNameWithoutExtension(file);
            String suffix = MoreFiles.getFileExtension(file);
            suffix = suffix.isEmpty() ? "" : "." + suffix;

            try {
                Files.createDirectories(idxFullPath);
                SymLinkUtils.allocateSymbolicLink(symLinkTgtAbsFile, idxFullPath, prefix, suffix);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void delete(Node g, Node s, Node p, Node o) {
//        Node g = quad.getGraph();
//        Node s = quad.getSubject();
//        Node p = quad.getPredicate();
//        Node o = quad.getObject();
        if (evaluateFind(s, p, o) != null) {
//            String idxIri = o.getURI();
//            Path idxRelPath = UriToPathUtils.resolvePath(idxIri);
            Path idxRelPath = objectToPath.apply(o);
            Path idxFullPath = basePath.resolve(idxRelPath);

// Should we sanity check that the symlink refers to the exact same target
// as it would if we created it from the quad?
//            String tgtIri = g.getURI();
//            Path tgtRelPath = syncedGraph.getRelPathForIri(tgtIri);
            String tgtFileName = syncedGraph.getFilename();

            Path symLinkSrcFile = idxFullPath.resolve(tgtFileName);
//            Path symLinkTgtFile = tgtRelPath.resolve(tgtFileName);


            try {
                boolean isSymlink = Files.isSymbolicLink(symLinkSrcFile);
                if (isSymlink) {
                    Files.delete(symLinkSrcFile);
                    // TODO Delete empty directory
                    // FileUtils.deleteDirectoryIfEmpty(basePath, symLinkSrcFile.getParent());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


//    protected void onPreCommit(DatasetGraphDiff diff) {
//        diff.getRemoved().find(Node.ANY, Node.ANY, propertyNode, Node.ANY)
//            .forEachRemaining(this::processRemoval);
//
//        diff.getAdded().find(Node.ANY, Node.ANY, propertyNode, Node.ANY)
//            .forEachRemaining(this::processAddition);
//    }
//

    public Float evaluateFind(Node s, Node p, Node o) {
        Float result = propertyNode.equals(p) && (o != null && objectToPath.apply(o) != null)
                ? Float.valueOf(1.0f)
                : null;
        return result;
    }


    public Iterator<Node> listGraphNodes(Node s, Node p, Node o) {
        if (evaluateFind(s, p, o) == null) {
            throw new RuntimeException("Index is not suitable for lookups with predicate " + p);
        }

//        String iri = o.getURI();
//        Path relPath = UriToPathUtils.resolvePath(iri);
        Path relPath = objectToPath.apply(o);
        //Path relPath = syncedGraph.getRelPathForIri(tgtIri);
        String fileName = syncedGraph.getFilename();

        Path file = Paths.get(fileName);
        String prefix = MoreFiles.getNameWithoutExtension(file);
        String suffix = MoreFiles.getFileExtension(file);
        suffix = suffix.isEmpty() ? "" : "." + suffix;

//        Path symLinkTgtFile = relPath.resolve(fileName);


        Path symLinkSrcPath = basePath.resolve(relPath);
        Map<Path, Path> symLinkTgtPaths;
        try {
            symLinkTgtPaths = Files.exists(symLinkSrcPath)
                    ? SymLinkUtils.readSymbolicLinks(symLinkSrcPath, prefix, suffix)
                    : Collections.emptyMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Load all graphs that are linked to and call find on the union

        IteratorConcat<Node> result = new IteratorConcat<>();
        for (Entry<Path, Path> srcToTgt : symLinkTgtPaths.entrySet()) {
//            Path absSymLinkTgt = srcToTgt.getValue();
//            Path tgtRelPath;
//            try {
                Path absTgt = SymLinkUtils.resolveSymLinkAbsolute(srcToTgt.getKey(), srcToTgt.getValue());
                Path tgtRelFile = syncedGraph.getBasePath().relativize(absTgt);

                // Get the path (without the filename)
                Path tgtRelPath = tgtRelFile.getParent();
//            } catch (IOException e1) {
//                throw new RuntimeException(e1);
//            }
            Entry<Path, Dataset> e = syncedGraph.getOrCreate(tgtRelPath);

            Iterator<Node> it = e.getValue().asDatasetGraph().listGraphNodes();
            result.add(it);
        }

        return result;
//        CatalogResolverFilesystem.allocateSymbolicLink(rawTarget, rawSourceFolder, baseName)
    }

}
