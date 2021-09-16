package org.aksw.jena_sparql_api.http.domain.api;

import java.util.List;
import java.util.Set;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView(RdfEntityInfo.class)
@RdfType("eg:EntityInfo")
public interface RdfEntityInfoDefault
    extends RdfEntityInfo
{
    @IriNs("eg")
    @Override
    List<String> getContentEncodings();

    @IriNs("eg")
    @Override
    String getContentType();

//    @IriNs("eg")
//    @Override
//    Long getContentLength();

    /**
     * Charset, such as UTF-8 or ISO 8859-1
     *
     * @return
     */
    @IriNs("eg")
    @Override
    String getCharset();

    /**
     * The set of language tags for which the content is suitable.
     *
     * @return
     */
    @IriNs("eg")
    @Override
    Set<String> getLanguageTags();


    @IriNs("dct")
    @Override
    Set<String> getConformsTo();


    @IriNs("eg")
    @Override
    Set<Checksum> getHashes();
}
