package org.aksw.dcat.jena.domain.api;

import java.util.Collection;

import org.aksw.dcat.util.ObjectUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;

/**
 * Binding of the core to jena
 *
 * @author raven Apr 9, 2018
 *
 */
public interface DcatDataset
    extends DcatEntity, DcatDatasetCore
{
    default <T extends Resource> Collection<T> getDistributions(Class<T> clazz) {
        return new SetFromPropertyValues<>(this, DCAT.distribution, clazz);
    }

    default Collection<? extends DcatDistribution> getDistributions() {
        return getDistributions(DcatDistribution.class);
    }

    public static String getLabel(DcatDataset ds) {
        String result = ObjectUtils.coalesce(
                ds::getTitle,
                ds::getIdentifier,
                () -> ds.isURIResource()
                    ? ds.getURI()
                    : Integer.toString(System.identityHashCode(ds)));

        return result;
    }
}
