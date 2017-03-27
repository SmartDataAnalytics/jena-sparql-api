package org.aksw.jena_sparql_api.sparql.ext.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_EncodeForQsa extends FunctionBase1 {
    /**
     * Source:
     * http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-
     * encodeuricomponent-that-produces-identical-outpu
     *
     * Encodes the passed String as UTF-8 using an algorithm that's compatible
     * with JavaScript's <code>encodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s
     *            The String to be encoded
     * @return the encoded String
     */
    public static String encodeURIComponent(String s) {
        String result = null;

        try {
            result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!").replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(").replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;

        if(nv.isString()) {
            String str = nv.getString();
            String tmp = encodeURIComponent(str);
            result = NodeValue.makeString(tmp);
        } else {
            result = nv;
        }

        return result;
    }
}
