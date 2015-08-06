package org.aksw.jena_sparql_api.batch.processor;

import java.util.regex.Pattern;

import com.google.common.base.Function;

/**
 * 
 * @author raven
 *
 */
public class FN_ReplaceLastNsPart
    implements Function<String, String>
{
    private String replacement;
    
    @Override
    public String apply(String arg0) {
        
        // Split by the last hash or slash
        
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public static void replaceLastNsPart(String uri, String replacement) {
        
        
        // Find the last # or / that is not preceeded by a sequence of whitespaces
        Pattern.compile("/\\s*$");
        Pattern.compile("#\\s*$");
        
        
        int a = uri.lastIndexOf('#');
        int b = uri.lastIndexOf('/');
        int i = Math.max(a, b);
        
        
    }
}
