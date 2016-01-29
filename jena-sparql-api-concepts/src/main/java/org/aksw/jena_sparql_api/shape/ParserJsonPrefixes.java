package org.aksw.jena_sparql_api.shape;

import java.util.Map;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

public class ParserJsonPrefixes {
    public static PrefixMapping parse(Object json) {
        PrefixMapping result = new PrefixMappingImpl();
        parse(result, json);
        return result;
    }


    public static PrefixMapping parse(PrefixMapping result, Object json) {
        if(json == null) {
            // nothing to do
        } else if(json instanceof Map) {
            Map<String, String> map = (Map<String, String>)json;
            parse(result, map);
        }
        return result;
    }
    
    public static PrefixMapping parse(PrefixMapping result, Map<String, String> map) {
        result.setNsPrefixes(map);
        return result;
    }
}
