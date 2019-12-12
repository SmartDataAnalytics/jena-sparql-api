package org.aksw.jena_sparql_api.utils.io;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.lang.LangNQuads;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.ParserProfileStd;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.apache.jena.sparql.core.Quad;

public class NTripleUtils {
	
    // Adjustment from RiotLib to use IRIResolver.createNoResolve()
    public static ParserProfile permissiveProfile() {
        return new ParserProfileStd(RiotLib.factoryRDF(), 
        							ErrorHandlerFactory.errorHandlerWarn,
                                    IRIResolver.createNoResolve(),
                                    PrefixMapFactory.createForInput(),
                                    RIOT.getContext().copy(),
                                    false, false);
    }

	public static ParserProfile profile = permissiveProfile();

	/**
	 * Parse the first triple from a given string.
	 * It is recommended for the string to not have any trailing data.
	 * 
	 * @param str
	 * @return
	 */
	public static Triple parseNTriplesString(String str)  {
		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(str);
		LangNTriples parser = new LangNTriples(tokenizer, profile, null);
		Triple result = parser.next();

		/*
		Triple result;
		try(InputStream is = new ByteArrayInputStream(str.getBytes())) {			
			Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(is);
			LangNTriples parser = new LangNTriples(tokenizer, profile, null);
			result = parser.next();
		} catch(Exception e) {
			throw new RuntimeException("Error parsing '" + str + "'", e);
		}
		*/
		
		return result;
	}
	
	/**
	 * Parse the first quad from a given string.
	 * It is recommended for the string to not have any trailing data.
	 * 
	 * @param str
	 * @return
	 */
	public static Quad parseNQuadsString(String str)  {
		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(str);
		LangNQuads parser = new LangNQuads(tokenizer, profile, null);
		Quad result = parser.next();
		
		return result;
	}
}
