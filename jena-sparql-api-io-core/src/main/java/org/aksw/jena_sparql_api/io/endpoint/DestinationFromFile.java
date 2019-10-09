package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Path;

public class DestinationFromFile
	implements FilterExecution
{
	protected Path path;

	@Override
	public boolean isFileDestination() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FilterConfig transferTo(FilterEngine engine) {
		engine.forInput(path);
	}

	@Override
	public FileWritingProcess create() {
		// TODO Auto-generated method stub
		return null;
	}

}
