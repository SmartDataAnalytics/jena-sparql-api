package org.aksw.jena_sparql_api.data_client;

import java.nio.file.Path;

public class ModelEntityFile
	implements ModelEntity
{
	protected Path path;

	@Override
	public boolean isMemory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ModelEntityMemory asMemory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ModelEntityFile asFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUrl() {
		// TODO Auto-generated method stub
		return false;
	}
}
