package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Block {
	ByteBuffer newBuffer();


	Block nextBlock() throws IOException;
	Block prevBlock() throws IOException;
	
	long blockSize();
	
//	Reference<Block> aquirePrevBlock();
//	Reference<Block> aquireNextBlock();
}
