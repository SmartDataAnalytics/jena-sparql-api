package org.aksw.commons.rx.cache.range;

/** Util class for improved performance on sequential inserts across pages */
class SinkForAppending<T> {
	protected PageManager<T> pageManager;
	protected long nextInsertOffset;

	// Cached attributes
	protected Page<T> currentPage = null;
	protected int offsetInPage;

	
	public SinkForAppending(PageManager<T> pageManager, long nextInsertOffset) {
		this.pageManager = pageManager;
		this.nextInsertOffset = nextInsertOffset;

		int pageSize = pageManager.getPageSize();
		long pageId = nextInsertOffset / pageSize;
		currentPage = pageManager.getPage(pageId);			
		offsetInPage = (int)(nextInsertOffset % (long)pageSize);
	}
	
	public void add(T item) {
		int capacity = currentPage.getKnownSize();
		if (!(offsetInPage < capacity)) {
			currentPage = currentPage.getNextPage();
			offsetInPage = 0;
		}		
		
		currentPage.set(offsetInPage, item);
		++offsetInPage;
		++nextInsertOffset;
	}
	
	public void flush() {
		
	}
	
	public void close() {
		
	}
}