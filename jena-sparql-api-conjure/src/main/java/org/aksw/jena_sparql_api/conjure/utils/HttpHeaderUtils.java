package org.aksw.jena_sparql_api.conjure.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.collectors.CollectorUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.google.common.collect.Maps;
import com.google.common.net.MediaType;

public class HttpHeaderUtils {

    public static Entry<String, String> toEntry(Header header) {
        Entry<String, String> result = Maps.immutableEntry(
                header.getName(),
                header.getValue());
        return result;
    }

    public static Stream<Entry<String, String>> toEntries(Header[] headers) {
        return Arrays.asList(headers).stream().map(HttpHeaderUtils::toEntry);
    }


    public static Header[] mergeHeaders(Header[] headers, String name) {
        List<Header> affectedHeaders = streamHeaders(headers, name)
                .collect(Collectors.toList());

        List<HeaderElement> parts = getElements(affectedHeaders.toArray(new Header[] {}), name)
                .collect(Collectors.toList());

        String mergedStr = parts.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(","));

        Header[] result = new Header[headers.length - (affectedHeaders.isEmpty() ? 0 : affectedHeaders.size() - 1)];

        boolean isHeaderEmitted = false;
        for(int i = 0, j = 0; i < headers.length; ++i) {
            Header h = headers[i];
            String headerName = h.getName();

            if(headerName.equalsIgnoreCase(name)) {
                if(!isHeaderEmitted) {
                    result[j++] = new BasicHeader(headerName, mergedStr);
                    isHeaderEmitted = true;
                }
                // else skip
            } else {
                result[j++] = headers[i];
            }
        }

        return result;
    }

    public static float qValueOf(HeaderElement h) {
        float result = Optional.ofNullable(h.getParameterByName("q"))
                        .map(NameValuePair::getValue)
                        .map(Float::parseFloat)
                        .orElse(1.0f);
        return result;
    }

    public static Stream<Header> streamHeaders(Header[] headers) {
        Stream<Header> result = headers == null ? Stream.empty() :
            Arrays.asList(headers).stream();

        return result;
    }

    public static Stream<Header> streamHeaders(Header[] headers, String name) {
        Stream<Header> result = streamHeaders(headers)
                .filter(h -> h.getName().equalsIgnoreCase(name));

        return result;
    }

    public static Stream<HeaderElement> getElements(Header[] headers) {
        Stream<HeaderElement> result = streamHeaders(headers)
                .flatMap(h -> Arrays.asList(h.getElements()).stream());

        return result;
    }

    public static Stream<HeaderElement> getElements(Header[] headers, String name) {
        Stream<HeaderElement> result = streamHeaders(headers)
                .filter(Objects::nonNull)
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .flatMap(h -> Arrays.asList(h.getElements()).stream());

        return result;
    }

    /**
     * TODO Ensure the result is stable; the javadoc for .sorted does not seem to guarantee this
     *
     * @param headers
     * @param name
     * @return A linked hash map with items inserted in the order of their q value
     */
    public static Map<String, Float> getOrderedValues(Header[] headers, String name) {
        Map<String, Float> result = getElements(headers, name)
            .map(e -> Maps.immutableEntry(e.getName(), qValueOf(e)))
            .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
            .collect(CollectorUtils.toLinkedHashMap(Entry::getKey, Entry::getValue));
        return result;
    }


    public static String getValueOrNull(Header header) {
        List<String> values = header == null
                ? null
                : getValues(new Header[] {header}, header.getName());

        if(values != null && values.size() > 1) {
            throw new RuntimeException("At most 1 value expected, got: " + values);
        }

        String result = values == null ? null : values.get(0);
        return result;
    }


    public static String getValue(Header[] headers, String name) {
        List<String> contentTypes = getValues(headers, name);
        if(contentTypes.size() != 1) {
            throw new RuntimeException("Exactly one content type expected, got: " + contentTypes);
        }

        return contentTypes.get(0);
    }

    public static List<String> getValues(Header header, String name) {
        List<String> result = getValues(new Header[] { header }, name);
        return result;
    }

    public static List<String> getValues(Header[] headers, String name) {
        List<String> result = getElements(headers, name)
            .map(HeaderElement::getName)
            .collect(Collectors.toList());

        return result;
    }

    public static RdfEntityInfo copyMetaData(HttpEntity src, RdfEntityInfo tgt) {
        tgt = tgt != null
                ? tgt
                : ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class);

        List<String> encodings = getValues(src.getContentEncoding(), HttpHeaders.CONTENT_ENCODING);
        String ct = getValueOrNull(src.getContentType());

        tgt.setContentType(ct);
        tgt.setContentEncodings(encodings);
//		tgt.setContentLength(src.getContentLength());

        return tgt;
    }



    // Bridge between rdf model and apache http components
    public static Header[] toHeaders(RdfEntityInfo info) {
        Header[] result = new Header[] {
            // TODO Add charset argument to content type header if info.getCharset() is non null
            new BasicHeader(HttpHeaders.CONTENT_TYPE, info.getContentType()),
            new BasicHeader(HttpHeaders.CONTENT_ENCODING, info.getEncodingsAsHttpHeader())
        };

        return result;
    }

    // TODO Move to some jena http utils



    public static List<MediaType> supportedMediaTypes() {
        Collection<Lang> langs = RDFLanguages.getRegisteredLanguages();
        List<MediaType> result = supportedMediaTypes(langs);
        return result;
    }

    public static List<String> langToContentTypes(Lang lang) {
        List<String> result = Stream.concat(
                Stream.of(lang.getContentType().getContentTypeStr()),
                lang.getAltContentTypes().stream())
                .distinct()
                .collect(Collectors.toList());

        return result;
    }


    public static List<MediaType> langToMediaTypes(Lang lang) {
        List<MediaType> result = langToContentTypes(lang).stream()
                .map(MediaType::parse)
                .collect(Collectors.toList());

        return result;
    }

    public static List<MediaType> supportedMediaTypes(Collection<Lang> langs) {
        List<MediaType> types = langs.stream()
                // Models can surely be served using based languages
                // TODO but what about quad based formats? I guess its fine to serve a quad based dataset
                // with only a default graph
                //.filter(RDFLanguages::isTriples)
                .flatMap(lang -> langToMediaTypes(lang).stream())
                .collect(Collectors.toList());
        return types;
    }

}
