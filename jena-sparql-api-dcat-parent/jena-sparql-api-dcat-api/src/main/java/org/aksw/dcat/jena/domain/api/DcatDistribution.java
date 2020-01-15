package org.aksw.dcat.jena.domain.api;

public interface DcatDistribution
	extends DcatEntity, DcatDistributionCore
{
//	Collection<Resource> getAccessResources();
//	Collection<Resource> getDownloadResources();
//	
//	default Collection<String> getAccessUrls() {
//		Collection<String> result = new CollectionFromConverter<>(getAccessResources(),
//				Converter.from(getModel()::createResource, Resource::getURI));
//		return result;
//	}
//
//	default Collection<String> getDownloadUrls() {
//		Collection<String> result = new CollectionFromConverter<>(getDownloadResources(),
//				Converter.from(getModel()::createResource, Resource::getURI));
//		return result;
//	}
	
//	default SpdxChecksum getChecksum() {
//		return null;
//		//ResourceUtils.getProperty(this, Spdx.ge)
//	}
}
