package org.aksw.dcat.ap.domain.api;

public interface ChecksumCore {
	String getAlgorithm();
	ChecksumCore setAlgorithm(String algorithm);
	
	String getChecksum();
	ChecksumCore setChecksum(String checksum);
}
