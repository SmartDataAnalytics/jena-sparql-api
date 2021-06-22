package org.aksw.jena_sparql_api.http.domain.api;

import java.util.Collection;
import java.util.List;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.rx.entity.EntityInfo;
import org.apache.jena.rdf.model.Resource;

public interface RdfEntityInfo
    extends Resource, EntityInfo
{
    RdfEntityInfo setContentEncodings(List<String> enocdings);
    RdfEntityInfo setContentType(String contentType);
    RdfEntityInfo setCharset(String charset);
//	RdfEntityInfo setContentLength(Long length);

    Collection<Checksum> getHashes();

//	@ToString
//	default $toString() {
//
//	}

    default Checksum getHash(String algo) {
        Checksum result = getHashes().stream()
            .filter(x -> algo.equalsIgnoreCase(x.getAlgorithm()))
            .findAny()
            .orElse(null);
        return result;
    }
}
