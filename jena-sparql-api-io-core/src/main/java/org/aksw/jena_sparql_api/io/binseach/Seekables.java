package org.aksw.jena_sparql_api.io.binseach;

public class Seekables {
	public static Seekable requestSeekableFor(PageManager pageManager, long pos) {
		PageNavigator result = new PageNavigator(pageManager);
		result.setPos(pos);
		return result;
	}
	

}
