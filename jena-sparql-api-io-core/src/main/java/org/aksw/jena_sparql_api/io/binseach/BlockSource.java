package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public interface BlockSource {
    Block contentAtOrBefore(long pos) throws IOException;
    Block contentAtOrAfter(long pos) throws IOException;

//	ByteBuffer firstContent();
//	ByteBuffer lastContent();

//	ByteBuffer getChannelForPos(long pos) throws IOException;
    long size() throws IOException;
}
