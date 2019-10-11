package org.aksw.jena_sparql_api.io.binseach;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;

public class NTripleUtils {
	public static Triple parseNtripleString(String str)  {
		Triple result;
		try(InputStream is = new ByteArrayInputStream(str.getBytes())) {			
			ParserProfile profile = RiotLib.dftProfile();
			Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(is);
			LangNTriples parser = new LangNTriples(tokenizer, profile, null);
			result = parser.next();
		} catch(Exception e) {
			throw new RuntimeException("Error parsing '" + str + "'", e);
		}
		
		return result;
	}
}
