package org.aksw.jena_sparql_api.web.utils;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

public class AuthenticatorUtils {
    public static HttpAuthenticator parseAuthenticator(HttpServletRequest req) {
        HttpAuthenticator result = null;
        /*
        Enumeration<String> e = req.getHeaderNames();
        while(e.hasMoreElements()) {
            String name = e.nextElement();
            System.out.println(name + ": " + req.getHeader(name));
        }*/

        String authStr = StringUtils.coalesce(
                req.getHeader("Authorization"),
                req.getHeader("WWW-Authenticate"));

        if(authStr != null && authStr.startsWith("Basic")) {
            // authStr: Basic base64credentials
            String base64Credentials = authStr.substring("Basic".length()).trim();

            // credentials = username:password
            String credentials = new String(DatatypeConverter.parseBase64Binary(base64Credentials));
            final String[] values = credentials.split(":", 2);

            if(values.length == 2) {
                String username = values[0];
                char[] password = values[1].toCharArray();

                result = new SimpleAuthenticator(username, password);
            } else {
                throw new RuntimeException("Invalid header - got: " + Arrays.asList(values));
            }
        }

        return result;
    }
}
