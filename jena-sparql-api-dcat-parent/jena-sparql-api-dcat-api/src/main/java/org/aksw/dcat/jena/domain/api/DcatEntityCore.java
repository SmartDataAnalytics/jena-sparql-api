package org.aksw.dcat.jena.domain.api;

public interface DcatEntityCore {
//	String getCkanId();
//	void setCkanId(String id);
	
	// Name is a public identifier; id is a internal identifier (e.g. ckan)
	String getIdentifier();
	void setIdentifier(String name);

	String getTitle();
	void setTitle(String title);

	String getDescription();
	void setDescription(String description);
}
