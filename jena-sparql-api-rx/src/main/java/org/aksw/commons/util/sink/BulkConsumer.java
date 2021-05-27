package org.aksw.commons.util.sink;

public interface BulkConsumer
{
	void accept(Object arrayWithItemsOfTypeT, int offset, int length);
}
