package org.aksw.commons.io.seekable.impl;

import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.block.impl.PageNavigator;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.io.seekable.api.SeekableSource;

public class SeekableSourceFromPageManager
    implements SeekableSource
{
    protected PageManager pageManager;

    public SeekableSourceFromPageManager(PageManager pageManager) {
        super();
        this.pageManager = pageManager;
    }

    @Override
    public boolean supportsAbsolutePosition() {
        return true;
    }

    @Override
    public Seekable get(long pos) {
        PageNavigator result = new PageNavigator(pageManager);

        result.setPos(pos);
        return result;
    }

    @Override
    public long size() {
        long result = pageManager.getEndPos();
        return result;
    }

}
