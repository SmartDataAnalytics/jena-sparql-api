package org.aksw.jena_sparql_api.io.binseach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.block.api.BlockSource;
import org.aksw.commons.io.block.impl.BlockSources;
import org.aksw.commons.io.seekable.impl.SeekableFromBlock;
import org.aksw.commons.util.ref.Ref;

import com.google.common.primitives.Ints;

public class BinarySearchOnBlockSource
    implements BinarySearcher
{
    protected BlockSource blockSource;
    protected AutoCloseable closeAction;

    public BinarySearchOnBlockSource(BlockSource blockSource, AutoCloseable closeAction) {
        super();
        this.blockSource = blockSource;
        this.closeAction = closeAction;
    }

    @Override
    public InputStream search(byte[] prefix) throws IOException {
        InputStream result;

        Ref<? extends Block> blockRef;
        if(prefix == null || prefix.length == 0) {
            blockRef = blockSource.contentAtOrAfter(0, true);
        } else {
            long maxBlockOffset = blockSource.size();
            blockRef = BlockSources.binarySearch(blockSource, 0, maxBlockOffset, (byte)'\n', prefix);
        }

        if(blockRef == null) {
            result = new ByteArrayInputStream(new byte[0]);
        } else {

            Block block = blockRef.get();

//            System.out.println("Block match: " + block.getOffset());
//            try(InputStream in = Channels.newInputStream(block.newChannel())) {
//                System.out.println("Block start:");
//                MainPlaygroundScanFile.printLines(in, 5);
//            }

            // System.out.println("Block offset: " + block.getOffset());

            // Load the block full + extra bytes up to the start of the first record in the
            // next block


            int extraBytes = 0;
            BlockIterState it = BlockIterState.fwd(true, blockRef.acquire(), false);
            while(it.hasNext()) {
                it.advance();
                try(SeekableFromBlock seekable = new SeekableFromBlock(it.blockRef.acquire(), 0, 0)) {
                    boolean found = seekable.posToNext((byte)'\n');
                    if(found) {
                        extraBytes = Ints.checkedCast(seekable.getPos());
                        it.closeCurrent();
                        break;
                    }
                }
            }
            // This extra close in case no match was found is ugly - refactor.
            it.closeCurrent();

            // extraBytes = 0;
//            System.out.println("Extra bytes: " + extraBytes);

            long blockSize = block.length();
//            System.out.println("Block size: " + blockSize);
            long maxPos = blockSize + extraBytes;

            SeekableFromBlock decodedView = new SeekableFromBlock(blockRef, 0, 0, Long.MIN_VALUE, maxPos);


            if(prefix == null || prefix.length == 0) {
                decodedView.setPos(0);

                result = Channels.newInputStream(decodedView);

            } else {
                long findPos = decodedView.binarySearch(-1, maxPos, (byte)'\n', prefix);

                if(findPos == Long.MIN_VALUE) {
                    // System.out.println("No pos found in block");
                    result = new ByteArrayInputStream(new byte[0]);
                } else {
                    // System.out.println(findPos);


                    // Seekable continuousView = new SeekableFromBlock(blockRef, (int)findPos, findPos);


                    long start = BinarySearchOnSortedFile.getPosOfFirstMatch(decodedView, (byte)'\n', prefix);
                    // Move past the delimiter
                    decodedView.nextPos(1);

                    BinSearchScanState state = new BinSearchScanState();
                    state.firstDelimPos = start;
                    state.matchDelimPos = findPos;
                    state.prefixBytes = prefix;
                    state.size = Long.MAX_VALUE;


                    result = BinarySearchOnSortedFile.newInputStream(decodedView, state);
                }
            }
        }

//        Path tmp = Paths.get("/tmp/debugging-binsearch.dat");
//        Files.copy(result, tmp, StandardCopyOption.REPLACE_EXISTING);
//        result = Files.newInputStream(tmp);

        return result;
    }

    @Override
    public void close() throws Exception {
        if(this.closeAction != null) {
            closeAction.close();
        }
    }
}
