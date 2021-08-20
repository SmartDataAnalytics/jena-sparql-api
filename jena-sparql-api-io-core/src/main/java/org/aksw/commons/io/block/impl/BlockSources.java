package org.aksw.commons.io.block.impl;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.block.api.BlockSource;
import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.seekable.api.SeekableSource;
import org.aksw.commons.io.seekable.impl.SeekableFromBlock;
import org.aksw.commons.io.seekable.impl.SeekableSourceFromPageManager;
import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.io.binseach.BinarySearchOnBlockSource;
import org.aksw.jena_sparql_api.io.binseach.BinarySearcher;
import org.aksw.jena_sparql_api.io.binseach.bz2.BlockSourceBzip2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockSources {
    private static final Logger logger = LoggerFactory.getLogger(BlockSources.class);


    public static BinarySearcher createBinarySearcherBz2(Path path) throws IOException {
        return createBinarySearcherBz2(path, PageManagerForFileChannel.DEFAULT_PAGE_SIZE);
    }

    public static BinarySearcher createBinarySearcherBz2(Path path, int pageSize) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        BinarySearcher result = createBinarySearcherBz2(channel, pageSize, true);
        return result;
    }

    public static BinarySearcher createBinarySearcherBz2(FileChannel fileChannel, int pageSize, boolean closeChannel) throws IOException {
        PageManager pageManager = PageManagerForFileChannel.create(fileChannel, pageSize);
        // long maxBlockOffset = pageManager.getEndPos();

        SeekableSource pagedSource = new SeekableSourceFromPageManager(pageManager);
        BlockSource blockSource = BlockSourceBzip2.create(pagedSource);

        BinarySearcher result = new BinarySearchOnBlockSource(blockSource, closeChannel ? fileChannel::close : null);
        return result;
    }


    public static BinarySearcher createBinarySearcherText(Path path) throws IOException {
        return createBinarySearcherText(path, PageManagerForFileChannel.DEFAULT_PAGE_SIZE);
    }

    public static BinarySearcher createBinarySearcherText(Path path, int pageSize) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        BinarySearcher result = createBinarySearcherText(channel, pageSize, true);
        return result;
    }

    public static BinarySearcher createBinarySearcherText(FileChannel fileChannel, int pageSize, boolean closeChannel) throws IOException {
        PageManager pageManager = PageManagerForFileChannel.create(fileChannel, pageSize);
        BinarySearcher result = new BinarySearchOnBlockSource(pageManager, closeChannel ? fileChannel::close : null);
        return result;
    }



    /**
     * Binary search over blocks
     *
     * Only examines the first record in the block to decide whether to look into the next or the previous one
     *
     * @param blockSource
     * @param min
     * @param max (exclusive)
     * @param delimiter
     * @param prefix
     * @return A reference to a block that may contain the key or null if no candidate block was found
     * @throws Exception
     */
    public static Ref<? extends Block> binarySearch(BlockSource blockSource, long min, long max, byte delimiter, byte[] prefix) throws IOException {
        logger.trace("Binary search in range [" + min + ", " + max + ")");
        if(min >= max) {
            return null;
        }

        Ref<? extends Block> result;

        long middlePos = (min + max) >> 1; // fast divide by 2

        // Find the start of the record in the block:
        // In the first block, this is position 0
        // otherwise this is the first delimiter
        Ref<? extends Block> blockRef = blockSource.contentAtOrBefore(middlePos, true);
        if(blockRef == null) {
            return null; //Long.MIN_VALUE;
        }
        Block block = blockRef.get();

        long pos = block.getOffset();
        if(pos < min) {
            return null;
        }

        //try(Seekable seekable = block.newChannel()) { //new SeekableFromChannelFactory(block)) {

        // For records larger than the block we'd need to create a seekable over
        // all blocks starting from the current block
        try(SeekableFromBlock seekable = new SeekableFromBlock(blockRef.acquire(), 0, 0)) {



            // TODO obtain correct flag
            boolean isNotFirstBlock = block.hasPrev();
            if(isNotFirstBlock) {
                    // TODO Issue below has been partly addressed - but we need to adjust the block and check
                    // whether its in range - so block.offset < max

                    // Handle the case where there is no delimiter
                    // FIXME This can actually happen with spatial datasets where polygons are
                    // larger than the bz2 block size!
                    // So in that case we'd have to scan the black backwards until we find one
//                System.out.println(seekable.getPos());
                seekable.posToNext(delimiter);
                seekable.nextPos(1);


                // seekable.getCurrentBlock();
//                }
            }
            int cmp = seekable.compareToPrefix(prefix);

            if(cmp == 0) {
                // We found an exact match
                // The chance for a hit here is proabaly very low, we could return a flag to avoid another round
                // of binary search - but the gain will be small
                // TODO Count on a test load how often we reach this case
                result = blockRef;
            } else if(cmp < 0) {
                // prefix is larger than the first key on the block
                // the search key may still be contained in this block
                // but check the upper half of the search range if there is another block
                //long lookupPos = pos + 1;
                try(Ref<? extends Block> nextBlockRef = blockSource.contentAtOrAfter(pos, false)) {

                    // If there is no further block it implies we are in the last block
                    if(nextBlockRef == null) {
                        // return it for further examination
                        result = blockRef;
                    } else {
                        long lowerBound = nextBlockRef.get().getOffset();
                        result = binarySearch(blockSource, lowerBound, max, delimiter, prefix);
                        if (result == null) {
                            result = blockRef;
                        } else {
                            closeWithRethrowAsIOException(blockRef);
                        }
                    }
                } catch (Exception e) {
                    throw new IOException(e);
                }
            } else { // if cmp > 0
                closeWithRethrowAsIOException(blockRef);
                // prefix is smaller than the first key of the block
                // search in lower half
                result = binarySearch(blockSource, min, pos, delimiter, prefix);
            }
        }

        return result;
    }

    public static void closeWithRethrowAsIOException(AutoCloseable obj) throws IOException {
        try {
            obj.close();
        } catch(Exception e) {
            throw new IOException(e);
        }
    }
}
