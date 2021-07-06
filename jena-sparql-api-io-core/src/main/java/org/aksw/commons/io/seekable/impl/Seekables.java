package org.aksw.commons.io.seekable.impl;

import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.block.impl.PageNavigator;
import org.aksw.commons.io.seekable.api.Seekable;

public class Seekables {
	public static Seekable requestSeekableFor(PageManager pageManager, long pos) {
		PageNavigator result = new PageNavigator(pageManager);
		result.setPos(pos);
		return result;
	}
	

}
