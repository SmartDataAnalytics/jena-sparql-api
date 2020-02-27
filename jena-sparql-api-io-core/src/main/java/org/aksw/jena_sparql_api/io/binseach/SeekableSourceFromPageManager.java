package org.aksw.jena_sparql_api.io.binseach;

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
