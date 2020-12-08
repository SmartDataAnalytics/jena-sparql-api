package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.util.Objects;

import org.aksw.jena_sparql_api.io.common.Reference;

/**
 * A helper iterator that automatically closes
 * the previous item when next() is called.
 *
 * Actually, we do not need next to return an object, instead it could
 * just set properties directly:
 *
 * IterState.advance();
 * IterState.closeCurrent();
 * IterState.current();
 *
 * @author raven
 *
 */
public class BlockIterState {
//    implements Iterator<OpenBlock> {

//    protected OpenBlock current;
    public Reference<? extends Block> blockRef;
    public Block block;
    public Seekable seekable;


    protected boolean yieldSelf;
    protected boolean skipFirstClose;
    protected boolean isFwd;

    public BlockIterState(boolean yieldSelf, Reference<? extends Block> blockRef, Seekable seekable, boolean isFwd) {
        // this.current = new OpenBlock(blockRef, seekable);
        Objects.requireNonNull(blockRef);

        this.blockRef = blockRef;
        this.block = blockRef.get();
        this.seekable = seekable;

        this.yieldSelf = yieldSelf;
        this.skipFirstClose = true;
        this.isFwd = isFwd;
    }

    public static BlockIterState fwd(boolean yieldSelf, Reference<? extends Block> blockRef, Seekable seekable) {
        return new BlockIterState(yieldSelf, blockRef, seekable, true);
    }

    public static BlockIterState fwd(boolean yieldSelf, Reference<? extends Block> blockRef) {
        return new BlockIterState(yieldSelf, blockRef, blockRef.get().newChannel(), true);
    }

    public static BlockIterState bwd(boolean yieldSelf, Reference<? extends Block> blockRef, Seekable seekable) {
        return new BlockIterState(yieldSelf, blockRef, seekable, false);
    }

    //@Override
    public boolean hasNext() {
        boolean result;
        try {
            result = yieldSelf // Return the block initial block first
                ? true
                :isFwd
                    ? block.hasNext()
                    : block.hasPrev();

        } catch (IOException e) {
            throw new RuntimeException();
        }
        return result;
    }


    public void closeCurrent() {
        if(!skipFirstClose) {
            try {
                blockRef.close();
                seekable.close();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void advance() {
        try {
            if(yieldSelf) {
                yieldSelf = false;
            } else {
                Reference<? extends Block> next = isFwd
                        ? block.nextBlock()
                        : block.prevBlock();

                if(next == null) {
                    // nothing to do
                } else {
                    closeCurrent();
                    skipFirstClose = false;

                    blockRef = next;
                    block = next.get();
                    seekable = block.newChannel();

                    //current = new OpenBlock(next, next.get().newChannel());
                    //result = current;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // return result;
    }

}