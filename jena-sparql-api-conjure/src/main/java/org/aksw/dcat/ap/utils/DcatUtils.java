package org.aksw.dcat.ap.utils;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;

public class DcatUtils {
	public static String getFirstDownloadUrlFromDistribution(Resource dcatDistribution) {
		String result = ResourceUtils.listPropertyValues(dcatDistribution, DCAT.downloadURL).toList().stream()
		        .filter(RDFNode::isURIResource)
		        .map(RDFNode::asResource)
		        .map(Resource::getURI)
		        .sorted()
		        .findFirst()
		        .orElse(null);

		return result;
	}
	
	public static String getFirstDownloadUrl(Resource dcatDataset) {
		String result = ResourceUtils.listPropertyValues(dcatDataset, DCAT.distribution, Resource.class).toList().stream()
	        .flatMap(d -> ResourceUtils.listPropertyValues(d, DCAT.downloadURL).toList().stream())
	        .filter(RDFNode::isURIResource)
	        .map(RDFNode::asResource)
	        .map(Resource::getURI)
	        .sorted()
	        .findFirst()
	        .orElse(null);

		return result;
	 }
}
