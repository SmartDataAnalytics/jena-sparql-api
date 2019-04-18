package org.aksw.jena_sparql_api.sparql.ext.util;

import org.aksw.jena_sparql_api.sparql.ext.csv.JenaExtensionCsv;
import org.aksw.jena_sparql_api.sparql.ext.fs.JenaExtensionFs;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaExtensionJson;
import org.aksw.jena_sparql_api.sparql.ext.sys.JenaExtensionSys;
import org.aksw.jena_sparql_api.sparql.ext.url.JenaExtensionUrl;
import org.aksw.jena_sparql_api.sparql.ext.xml.JenaExtensionXml;
import org.apache.jena.shared.PrefixMapping;

public class JenaExtensionUtil {
//    public static void registerAll() {
//        throw new RuntimeException("Not yet implemented, because we need to support configuration options");
//    }
    
    public static void addPrefixes(PrefixMapping pm) {
    	JenaExtensionJson.addPrefixes(pm);
    	JenaExtensionCsv.addPrefixes(pm);
    	JenaExtensionXml.addPrefixes(pm);
    	JenaExtensionUrl.addPrefixes(pm);
    	JenaExtensionFs.addPrefixes(pm);
    	JenaExtensionSys.addPrefixes(pm);
    }
}
