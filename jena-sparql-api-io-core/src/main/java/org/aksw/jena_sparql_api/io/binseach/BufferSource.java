package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public interface BufferSource {
	DecodedDataBlock contentBefore(long pos) throws IOException;
	DecodedDataBlock contentAfter(long pos) throws IOException;

	
	
//	ByteBuffer firstContent();
//	ByteBuffer lastContent();
	
//	ByteBuffer getChannelForPos(long pos) throws IOException;
	long size() throws IOException;
}
