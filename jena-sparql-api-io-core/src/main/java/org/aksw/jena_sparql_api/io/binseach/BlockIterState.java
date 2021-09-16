package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.util.ref.Ref;

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
    public Ref<? extends Block> blockRef;
    public Block block;
    public Seekable seekable;


    protected boolean yieldSelf;
    protected boolean skipFirstClose;
    protected boolean isFwd;

    public BlockIterState(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable, boolean isFwd, boolean skipFirstClose) {
        // this.current = new OpenBlock(blockRef, seekable);
        Objects.requireNonNull(blockRef);

        this.blockRef = blockRef;
        this.block = blockRef.get();
        this.seekable = seekable;

        this.yieldSelf = yieldSelf;
        this.skipFirstClose = skipFirstClose;
        this.isFwd = isFwd;
    }

    public static BlockIterState fwd(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable) {
        return new BlockIterState(yieldSelf, blockRef, seekable, true, true);
    }

    public static BlockIterState fwd(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable, boolean skipFirstClose) {
        return new BlockIterState(yieldSelf, blockRef, seekable, true, skipFirstClose);
    }

    public static BlockIterState fwd(boolean yieldSelf, Ref<? extends Block> blockRef, boolean skipFirstClose) {
        return new BlockIterState(yieldSelf, blockRef, blockRef.get().newChannel(), true, skipFirstClose);
    }

    public static BlockIterState bwd(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable) {
        return new BlockIterState(yieldSelf, blockRef, seekable, false, true);
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
                if (!blockRef.isClosed()) {
                    blockRef.close();
                }

                if (seekable.isOpen()) {
                    seekable.close();
                }
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
                Ref<? extends Block> next = isFwd
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