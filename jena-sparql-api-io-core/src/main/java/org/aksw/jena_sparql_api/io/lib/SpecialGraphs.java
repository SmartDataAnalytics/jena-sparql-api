package org.aksw.jena_sparql_api.io.lib;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.aksw.commons.io.block.impl.BlockSources;
import org.aksw.commons.io.block.impl.PageManagerForFileChannel;
import org.aksw.jena_sparql_api.io.binseach.BinarySearchOnSortedFile;
import org.aksw.jena_sparql_api.io.binseach.BinarySearcher;
import org.aksw.jena_sparql_api.io.binseach.GraphFromPrefixMatcher;
import org.apache.jena.graph.Graph;

public class SpecialGraphs {
    public static Graph fromSortedNtriplesFile(Path path) throws IOException {
        BinarySearcher binarySearcher = BinarySearchOnSortedFile.create(path);
        Graph result = new GraphFromPrefixMatcher(binarySearcher);
        return result;
    }

    public static Graph fromSortedNtriplesBzip2File(Path path) throws IOException {
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        BinarySearcher binarySearcher = BlockSources.createBinarySearcherBz2(fileChannel, PageManagerForFileChannel.DEFAULT_PAGE_SIZE, true);

        Graph result = new GraphFromPrefixMatcher(binarySearcher);
        return result;
    }
}
