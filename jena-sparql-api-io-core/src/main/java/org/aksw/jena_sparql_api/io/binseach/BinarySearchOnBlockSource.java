package org.aksw.jena_sparql_api.io.binseach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

import org.aksw.jena_sparql_api.io.common.Reference;

import com.google.common.primitives.Ints;

public class BinarySearchOnBlockSource
    implements BinarySearcher
{
    protected BlockSource blockSource;

    public BinarySearchOnBlockSource(BlockSource blockSource) {
        super();
        this.blockSource = blockSource;
    }

    @Override
    public InputStream search(byte[] prefix) throws IOException {
        InputStream result;

        long maxBlockOffset = blockSource.size();

        Reference<Block> blockRef = BlockSources.binarySearch(blockSource, 0, maxBlockOffset, (byte)'\n', prefix);
        if(blockRef == null) {
            result = new ByteArrayInputStream(new byte[0]);
        } else {

            Block block = blockRef.get();

            try(InputStream in = Channels.newInputStream(block.newChannel())) {
                System.out.println("Block start:");
                MainPlaygroundScanFile.printLines(in, 5);
            }

            // System.out.println("Block offset: " + block.getOffset());

            // Load the block full + extra bytes up to the start of the first record in the
            // next block


            int extraBytes = 0;
            BlockIterState it = BlockIterState.fwd(blockRef);
            while(it.hasNext()) {
                it.advance();
                try(SeekableFromBlock seekable = new SeekableFromBlock(it.blockRef, 0, 0)) {
                    boolean found = seekable.posToNext((byte)'\n');
                    if(found) {
                        extraBytes = Ints.checkedCast(seekable.getPos());
                        it.closeCurrent();
                        break;
                    }
                }
            }

            // extraBytes = 0;
            System.out.println("Extra bytes: " + extraBytes);

            long blockSize = block.length();
            System.out.println("Block size: " + blockSize);
            long maxPos = blockSize + extraBytes;

            SeekableFromBlock decodedView = new SeekableFromBlock(it.blockRef, 0, 0, maxPos);

            long findPos = decodedView.binarySearch(0, maxPos, (byte)'\n', prefix);

            if(findPos == Long.MIN_VALUE) {
                // System.out.println("No pos found in block");
                result = new ByteArrayInputStream(new byte[0]);
            } else {
                // System.out.println(findPos);


                // Seekable continuousView = new SeekableFromBlock(blockRef, (int)findPos, findPos);


                long start = BinarySearchOnSortedFile.getPosOfFirstMatch(decodedView, (byte)'\n', prefix);
                decodedView.setPos(start + 1);

                BinSearchScanState state = new BinSearchScanState();
                state.firstDelimPos = start;
                state.matchDelimPos = findPos;
                state.prefixBytes = prefix;
                state.size = Long.MAX_VALUE;


                result = BinarySearchOnSortedFile.newInputStream(decodedView, state);
            }
        }

        return result;
    }
}
