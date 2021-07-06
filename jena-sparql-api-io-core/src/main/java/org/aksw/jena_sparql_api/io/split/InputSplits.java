package org.aksw.jena_sparql_api.io.split;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.block.api.BlockSource;
import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.block.impl.PageManagerForFileChannel;
import org.aksw.commons.io.block.impl.PageNavigator;
import org.aksw.commons.util.ref.Ref;

public class InputSplits {
    public static List<Object> createInputSplits(BlockSource blockSource) throws Exception {
        long size = blockSource.size();
        int numBatches = 4;
        List<Long> rawSplits = createSplits(size, numBatches);
        List<Long> splits = new ArrayList<>();

        long priorOffset = -1;
        for(int i = 0; i < rawSplits.size(); ++i) {
            try(Ref<? extends Block> blockRef = blockSource.contentAtOrAfter(i, true)) {
                if(blockRef != null) {
                    // Ensure that no distinct split resolve to the same block - otherwise ignore
                    // those splits that are too close to each other
                    long offset = blockRef.get().getOffset();
                    if(offset != priorOffset) {
                        splits.add(i, offset);
                        priorOffset = offset;
                    }
                } else {
                    break;
                }
            }
        }

        // Now we need to adjust the byte range of a block so that it cleanly matches
        // record boundaries



        return null;

    }

    public static List<Long> createSplits(long size, int numBatches) {

        List<Long> result = new ArrayList<>();

        long chunkSize = size / numBatches;
        int remainder = (int)size % numBatches;

        Map<Integer, PageNavigator> navs = new HashMap<>();
        long start = 0;
        for(int i = 0; i < numBatches; ++i) {
            long extra = i < remainder ? 1 : 0;
            long end = start + chunkSize + extra;
//			System.out.println("Chunk: " + start + " -> " + end);
            start = end;
        }

        return result;
    }

    public static List<Object> createInputSplits(FileChannel fileChannel) throws IOException {

        PageManager pageManager = PageManagerForFileChannel.create(fileChannel);
//		PageManager pageManager = PageManagerForFileChannel.create(fileChannel,  128 * 1024 * 1024);
//		PageManager pageManager = new PageManagerForByteBuffer(ByteBuffer.wrap(str.getBytes()));

        long size = pageManager.size();
        //long size = fileChannel.size();
        int numChunks = 4; //32;
        boolean fwd = true;
        int numRuns = 10;

        long chunkSize = size / numChunks;
        int remainder = (int)size % numChunks;

        Map<Integer, PageNavigator> navs = new HashMap<>();
        long start = 0;
        for(int i = 0; i < numChunks; ++i) {
            long extra = i < remainder ? 1 : 0;
            long end = start + chunkSize + extra;
//			System.out.println("Chunk: " + start + " -> " + end);
            navs.put(i, new PageNavigator(pageManager, start, end));
            start = end;
        }

        return null;
    }
}
