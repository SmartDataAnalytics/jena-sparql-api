package org.aksw.jena_sparql_api.utils.io;

import java.lang.reflect.Field;

import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;

public class StreamRDFUtils {
    public static StreamRDF unwrap(StreamRDF result) {
        try {
            while (result instanceof StreamRDFWrapper) {
                Field field = StreamRDFWrapper.class.getDeclaredField("other");
                field.setAccessible(true);
                result = (StreamRDF)field.get(result);
                field.setAccessible(false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static StreamRDF wrapWithoutPrefixDelegation(StreamRDF other) {
    	return new StreamRDFWrapper(other) {
    		@Override
    		public void prefix(String prefix, String iri) {
    			// Do nothing
    		}
    	};
    }
}
