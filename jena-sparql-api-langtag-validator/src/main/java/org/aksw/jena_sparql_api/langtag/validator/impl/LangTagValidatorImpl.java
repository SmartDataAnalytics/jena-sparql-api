package org.aksw.jena_sparql_api.langtag.validator.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidationException;
import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.web.LangTag;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class LangTagValidatorImpl
	implements LangTagValidator
{
	public static final Property IANA_TYPE = ResourceFactory.createProperty("urn:Type");
	public static final Property IANA_SUBTAG = ResourceFactory.createProperty("urn:Subtag");
	
	/** Index of valid known values per component of a language tag */
	protected Multimap<Integer, String> index;
	
	public LangTagValidatorImpl(Multimap<Integer, String> index) {
		super();
		this.index = index;
	}

	public static Multimap<Integer, String> indexLangTagKnownValues(Model langRegistryModel) {
		Multimap<Integer, String> result = HashMultimap.create();
		
		Map<String, Integer> idxToType = new HashMap<>();
		idxToType.put("language", LangTag.idxLanguage);
		idxToType.put("script", LangTag.idxScript);
		idxToType.put("region", LangTag.idxRegion);
		idxToType.put("variant", LangTag.idxVariant);
		idxToType.put("extlang", LangTag.idxExtension);
		
		Property type = ResourceFactory.createProperty("urn:Type");
		
		Set<Resource> set = langRegistryModel.listSubjectsWithProperty(type)
				.mapWith(RDFNode::asResource).toSet();
		
		for (Resource tag : set) {
			String xtype = Optional.ofNullable(tag.getProperty(IANA_TYPE)).map(Statement::getString).orElse(null);
			String xsubtag = Optional.ofNullable(tag.getProperty(IANA_SUBTAG)).map(Statement::getString).orElse(null);
			Integer idx = idxToType.get(xtype);

			if (idx == null) {
				// System.err.println("Unknown type: " + xtype + " on " + tag);
				// Objects.requireNonNull(idx, "Failed to map " + tag);
				continue;
			}
			
			result.put(idx, xsubtag);			
		}

		return result;
	}

	@Override
	public boolean check(String langTag) {
		boolean result = check(langTag, index);
		return result;
	}

	@Override
	public void validate(String langTag) throws LangTagValidationException {
		validate(langTag, index, true);
	}
	
	public static LangTagValidatorImpl createDefault() {
		Model langTagModel = RDFDataMgr.loadModel("iana-language-subtag-registry.2021-02-16.raw.ttl");
		return create(langTagModel);
	}

	public static LangTagValidatorImpl create(Model langTagModel) {
		Multimap<Integer, String> index = indexLangTagKnownValues(langTagModel);
		return new LangTagValidatorImpl(index);
	}

	public static boolean check(
			String langTag,
			Multimap<Integer, String> index) {
		boolean result;
		try {
			result = validate(langTag, index, false);
		} catch (LangTagValidationException e) {
			// Never happens with flag set to false
			result = false;
		}
		
		return result;
	}

	
	public static boolean validate(
			String langTag,
			Multimap<Integer, String> index,
			boolean raiseException) throws LangTagValidationException {
		String[] parts = LangTag.parse(langTag);
		// System.out.println(Arrays.toString(parts));

		int[] knownIdxs = new int[] {LangTag.idxLanguage, LangTag.idxScript, LangTag.idxRegion, LangTag.idxVariant, LangTag.idxExtension };
		
		// Valid unless proven otherwise
		boolean result = true;
		
		if (parts == null) {
			result = false;
			if (raiseException) {
				throw new LangTagValidationException("Failed to parse: " + langTag);
			}
		} else {
			for (int i = 0; i < knownIdxs.length; ++i) {
				int partId = knownIdxs[i];
				String givenValue = parts[partId];
				
				if (givenValue == null) {
					continue;
				}
				
				Collection<String> knownValidValues = index.get(partId); 				
				
				boolean isValidValue = knownValidValues.contains(givenValue);
				
				if (!isValidValue) {
					result = false;
					
					// We could add a 'Did you mean ...?' mechanisms here; would require commons-text
					// for levenshtein distance
					
					if (raiseException) {
						throw new LangTagValidationException("Value '" + givenValue + "' is not known to be valid for part #" + partId); // + " valid values: " + new TreeSet<>(knownValidValues));
					}
					
					break;
				}
			}
		}
		
		return result;
	}
}
