package org.aksw.dcat.ap.utils;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class DcatDatasetCreation {
    /**
     * Create a simple DCAT Dataset from a url.
     *
     * The pattern is as follows:
     * <pre>
     * url#dataset
     *   dcat:distribution url#distribution .
     *
     * url#distribution
     *   dcat:downloadURL url
     * </pre>
     *
     * @param url
     * @return
     */
    public static DcatDataset fromDownloadUrl(String url) {
        Model model = ModelFactory.createDefaultModel();
        DcatDataset result = model.createResource(url + "#dataset").as(DcatDataset.class);

        DcatDistribution dist = model.createResource(url + "#distribution").as(DcatDistribution.class);
        result.getDistributions(DcatDistribution.class).add(dist);
        dist.setDownloadURL(url);

        return result;
    }
}
