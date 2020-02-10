package org.aksw.jena_sparql_api.conjure.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.collectors.CollectorUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.math.DoubleMath;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.google.common.net.MediaType;



public class ContentTypeUtils {

	// TODO Move to registry, use the RDF model for init
	protected static MapPair<String, String> ctExtensions = new MapPair<>();
	protected static MapPair<String, String> codingExtensions = new MapPair<>();

	
	public static Set<MediaType> compatibleContentTypes(MediaType range, Collection<MediaType> candidates) {
		Set<MediaType> result = candidates.stream()
			.filter(range::is)
			.collect(Collectors.toSet());
		
		return result;
	}
	
	
	public static String entryToHeaderValue(Entry<String, Float> e) {
		Float weight = e.getValue();
		
		boolean isOne = weight == null || DoubleMath.fuzzyEquals(1.0, weight, 0.001);
		String result = e.getKey() + (isOne ? "" : ";q=" + weight);
		return result;
	}
	
	public static int classifyLang(Lang lang) {
		int result = RDFLanguages.isTriples(lang) ? 3
				: RDFLanguages.isQuads(lang) ? 4
				: -1;

		return result;
	}
	
	/**
	 * Expands the accept header of a request with respect to registered languages
	 * 
	 * 
	 * 
	 * For each range r in the accept header
	 *   get all langs l and qvalues that that correspond to r
	 *   for each lang l get the set of content types
	 *     assign each content type the minimum qvalue 
	 * 
	 * 
	 * 
	 * @param headers
	 * @return
	 */
	public static Header[] expandAccept(Header[] headers) {
		
		Map<MediaType, Float> ranges = HttpHeaderUtils
				.getOrderedValues(headers, HttpHeaders.ACCEPT).entrySet().stream()
				.collect(CollectorUtils.toLinkedHashMap(e -> MediaType.parse(e.getKey()), Entry::getValue));

		
		//protected Map<String, String> ctToLang
		
		Map<MediaType, Float> expansions = new LinkedHashMap<>();

		List<MediaType> supportedMediaTypes = HttpHeaderUtils.supportedMediaTypes();
		//Collection<Lang> langs = RDFLanguages.getRegisteredLanguages();

		
		
		for(Entry<MediaType, Float> rangeEntry : ranges.entrySet()) {
			MediaType range = rangeEntry.getKey();
			Float weight = rangeEntry.getValue();
			
			Collection<MediaType> compatibles = compatibleContentTypes(range, supportedMediaTypes);
			for(MediaType mediaType : compatibles) {
//				if(mediaType.is(range)) {
				// get the lang for the media type
				Lang lang = RDFLanguages.nameToLang(mediaType.toString());
				if(lang != null) {
					
					int langClass = classifyLang(lang);

					List<Lang> expansionLangs = RDFLanguages.getRegisteredLanguages().stream()
						.filter(l -> classifyLang(l)  == langClass)
						.collect(Collectors.toList());
					
					
					// Get the media types not covered by any other range
					Collection<MediaType> langCts = expansionLangs.stream()
							.flatMap(l -> HttpHeaderUtils.langToMediaTypes(l).stream())
							.collect(Collectors.toList());
					
					// The set of extended cts are those that are not covered by any other range in the headers
					Collection<MediaType> newCts = langCts.stream()
							.filter(mt -> ranges.keySet().stream().noneMatch(r -> mt.is(r)))
							.collect(Collectors.toList());

					for(MediaType newCt : newCts) {
						expansions.put(newCt, weight);
					}
				}
			}
		}

		Map<MediaType, Float> sortedExp = expansions.entrySet().stream()
				.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
				.collect(CollectorUtils.toLinkedHashMap(Entry::getKey, Entry::getValue));
		
		String expansionStr = sortedExp.entrySet().stream()
				.map(e -> Maps.immutableEntry(e.getKey().toString(), e.getValue()))
				.map(ContentTypeUtils::entryToHeaderValue)
				.collect(Collectors.joining(","));

		Header[] tmp = new Header[headers.length + 1];
		
		System.arraycopy(headers, 0, tmp, 0, headers.length);
		tmp[headers.length] = new BasicHeader(HttpHeaders.ACCEPT, expansionStr);
		
		Header[] result = HttpHeaderUtils.mergeHeaders(tmp, HttpHeaders.ACCEPT);

		return result;		
	}
	
	static {
		for(Lang lang : RDFLanguages.getRegisteredLanguages()) {
			Collection<String> contentTypes = HttpHeaderUtils.langToContentTypes(lang);
			for(String contentType : contentTypes) {
//				String contentType = lang.getContentType().getContentType();
				String primaryFileExtension = Iterables.getFirst(lang.getFileExtensions(), null);
				if(primaryFileExtension != null) {
					ctExtensions.getPrimary().put(contentType, primaryFileExtension);
				}
				
				for(String fileExtension : lang.getFileExtensions()) {
					ctExtensions.getAlternatives().put(fileExtension, contentType);
				}
			}
		}
	
		ctExtensions.putPrimary(ContentType.APPLICATION_OCTET_STREAM.toString(), "bin");
		
		// TODO We need a registry for coders similar to RDFLanguages
		codingExtensions.putPrimary("gzip", "gz");
		codingExtensions.putPrimary("bzip2", "bz2");
	}
	
	
	// TODO Move 
	
	public static String toFileExtension(Header[] headers) {
		String result = toFileExtensionCt(headers) +
				toFileExtension(HttpHeaderUtils.getValues(headers, HttpHeaders.CONTENT_ENCODING));
		return result;
	}
		
	
	public static String toFileExtensionCt(Header[] headers) {
		String ct = HttpHeaderUtils.getValue(headers, HttpHeaders.CONTENT_TYPE);
		String result = ctExtensions.getPrimary().get(ct);
		Objects.requireNonNull(result, "Could not find file extension for content type: " + ct + " ; got: " + ctExtensions.getPrimary());
		result = "." + result;
		return result;		
	}
	
	public static String toFileExtension(List<String> codings) {
		List<String> parts = new ArrayList<>(codings.size());
		
		for(String coding : codings) {
			String part = Objects.requireNonNull(codingExtensions.getPrimary().get(coding));
			parts.add(part);
		}
		
		String result = parts.stream().collect(Collectors.joining("."));
		
		result = result.isEmpty() ? result : "." + result;
		return result;
	}

	
	public static String toFileExtension(RdfEntityInfo info) {
		Header[] headers = HttpHeaderUtils.toHeaders(info);
		String result = toFileExtension(headers);
		return result;
	}
	
	public static String toFileExtension(String contentType) {
		String result = Objects.requireNonNull(ctExtensions.getPrimary().get(contentType));
		result = result.isEmpty() ? result : "." + result;
		return result;
	}

	
	public static String toFileExtension(String contentType, List<String> codings) {
		List<String> parts = new ArrayList<>(1 + codings.size());
		
		String part = Objects.requireNonNull(ctExtensions.getPrimary().get(contentType));
		parts.add(part);
		
		for(String coding : codings) {
			part = Objects.requireNonNull(codingExtensions.getPrimary().get(coding));
			parts.add(part);
		}
		
		String result = parts.stream().collect(Collectors.joining("."));
		return result;
	}

	/**
	 * Attempts to get Content-type, Content-encoding from a given filename 
	 * 
	 * @param resultFile The non-null file that will be passed into the FileEntityEx result. Used for pragmatic reasons, as it seems to be the
	 * best Entity class that prevents us from having to roll our own.
	 * @param fileName
	 * @return
	 */
	public static RdfEntityInfo deriveHeadersFromFileExtension(String fileName) {
		// TODO This method expects a file name - not the file extension alone - so
		// an argument of x.ttl works, but just ttl will fail - fix that!S
		// TODO Should we remove trailing slashes?
		
		String contentType = null;
		List<String> codings = new ArrayList<>();
		

		String current = fileName;
		
		while(true) {
			String ext = com.google.common.io.Files.getFileExtension(current);
			
			if(ext == null) {
				break;
			}
			
			// Classify the extension - once it maps to a content type, we are done
			String coding = codingExtensions.getAlternatives().get(ext);
			String ct = ctExtensions.getAlternatives().get(ext);

			// Prior validation of the maps should ensure that at no point a file extension
			// denotes both a content type and a coding
			assert !(coding != null && ct != null) :
				"File extension conflict: '" + ext + "' maps to " + coding + " and " + ct;  
			
			if(coding != null) {
				codings.add(coding);
			}
			
			if(ct != null) {
				contentType = ct;//MediaType.parse(ct);
				break;
			}
			
			// both coding and ct were null - skip
			if(coding == null && ct == null) {
				break;
			}
			
			current = com.google.common.io.Files.getNameWithoutExtension(current);//current.substring(0, current.length() - ext.length());
		}
		
		RdfEntityInfo result = null;
		if(contentType != null) {
			result = ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class);
			result.setContentType(contentType);
			result.setContentEncodings(codings);
			
//			fileEntity = new FileEntityEx(resultFile);
//			fileEntity.setContentType(contentType);
//			if(codings.isEmpty()) {
//				// nothing to do
//			} else {
//				String str = codings.stream().collect(Collectors.joining(","));
//				fileEntity.setContentEncoding(str);
//			}
		}
		
		return result;

	}
}
