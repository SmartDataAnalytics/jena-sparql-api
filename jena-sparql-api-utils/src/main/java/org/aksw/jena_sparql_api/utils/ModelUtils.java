package org.aksw.jena_sparql_api.utils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.aksw.commons.collections.MultiMaps;
import org.aksw.commons.util.strings.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * @author Claus Stadler
 */
public class ModelUtils {


    /**
     * Extracts a mapping childClass -> parentClass from a given Model.
     * You can use TransitiveClosure.transitiveClosure for "inferring" the whole hierarchy.
     *
     * @param model
     * @return
     */
    public static Map<Resource, Set<Resource>> extractDirectSuperClassMap(Model model) {
        Map<Resource, Set<Resource>> result = new HashMap<Resource, Set<Resource>>();

        StmtIterator it = model.listStatements(null, RDFS.subClassOf, (RDFNode)null);
        while (it.hasNext()) {
            Statement stmt = it.next();

            // Skip "invalid" triples
            if(!(stmt.getObject() instanceof Resource))
                continue;

            MultiMaps.put(result, stmt.getSubject(), (Resource)stmt.getObject());
        }
        it.close();

        return result;
    }

    private static Logger logger = LoggerFactory.getLogger(ModelUtils.class);




    /**
     *
     * @param model
     * @param resource
     * @return
     */
    public static Model filterBySubject(Model model, Resource resource)
    {
        Iterator<Statement> it = model.listStatements(resource, (Property)null, (RDFNode)null);
        Model result = ModelFactory.createDefaultModel();

        result.setNsPrefixes(model.getNsPrefixMap());

        while(it.hasNext()) {
            result.add(it.next());
        }

        return result;
    }

    public static Model combine(Collection<Model> models)
    {
        Model result = ModelFactory.createDefaultModel();

        for(Model model : models) {
            result.add(model);
        }

        return result;
    }

    public static Model read(InputStream in, String lang)
        throws IOException
    {
        return read(ModelFactory.createDefaultModel(), in, lang);
    }

    /*
    public static void main(String[] args) throws IOException {
        read(new File("test.nt"));
    }
    */

    /**
     * FIXME Extend to Uris
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static Model read(File file)
        throws Exception
    {
        Collection<String> langs = null;

        // Auto detect language by file name extension
        String fileName = file.getPath().toLowerCase();

        for(Map.Entry<String, String> entry : Constants.extensionToJenaFormat.entrySet()) {
            if(fileName.endsWith(entry.getKey().toLowerCase())) {
                langs = Collections.singleton(entry.getValue());
                break;
            }
        }

        if(langs == null) {
            langs = new HashSet<String>(Constants.extensionToJenaFormat.values());
        }

        String logMessage = "Parsing file '" + fileName + "' with languages " + langs + ": ";
        Model result = null;
        for(String lang : langs) {
            FileInputStream in = new FileInputStream(file);
            try {
                result = read(in, lang);

                logMessage += " Success (" + lang + ")";
                break;
            } catch(Exception e) {
                //FIXME: Extend this method to return a mapping of language to exception

                if(langs.size() == 1) {
                    throw e;
                }
            }
            finally {
                if(in != null) in.close();
            }
        }

        if(result == null) {
            logMessage += " Failed. ";
        }

        logger.debug(logMessage);

        if(result == null) {
            throw new IOException("Unsupported file format");
        }

        return result;
    }


    public static Model read(File file, String lang)
        throws IOException
    {
        return read(new FileInputStream(file), lang);
    }


    public static Model read(Model model, InputStream in, String lang)
        throws IOException
    {
        try {
            model.read(in, null, lang);
        }
        finally {
            in.close();
        }

        return model;
    }

    public static Model read(Model model, File file, String lang)
        throws IOException
    {
        return read(model, new FileInputStream(file), lang);
    }


    public static Model write(Model model, File file)
        throws IOException
    {
        Map.Entry<String, String> extToLang = StringUtils.getMatchBySuffix(file.getPath(), Constants.extensionToJenaFormat);
        String lang = (extToLang == null) ? null : extToLang.getValue();

        return write(model, file, lang);
    }

    public static Model write(Model model, File file, String lang)
        throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        model.write(out, lang);
        out.close();

        return model;
    }

    public static String toString(Model model)
    {
        return toString(model, "N3");
    }

    public static String toString(Model model, RDFWriter writer)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /*
        OutputStreamWriter osw;
        try {
            osw = new OutputStreamWriter(baos, "UTF8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        writer.write(Model, osw, "");
        */

        writer.write(model, baos, "");

        return baos.toString();

    }

    public static String toString(Model model, String format)
    {
        if(model == null)
            return "null";

        RDFWriter writer = model.getWriter(format);

        return toString(model, writer);
    }


    /**
     * An efficient method for separating a URI into namespace and prefix,
     * given a set of arbitrary namespace prefixes.
     * Note: This method is mainly intended for a nice representation,
     * as a decomposition with arbitrary prefixes may not work with cerain RDF
     * serializations.
     *
     * (e.g. <http://ex.org/this/is/a/test> with prefix p:<http://ex.org/this>
     * becomes p:is/a/test)
     *
     *
     * @param uri
     * @param prefixMap
     * @return
     */
    public static String[] decompose(String uri, NavigableMap<String, String> prefixMap)
    {
        String prefix = "";
        String name = uri;

        //NavigableMap<String, String> candidates = prefixMap.headMap(uri, false).descendingMap();
        //Map.Entry<String, String> candidate = candidates.firstEntry();

        Map.Entry<String, String> candidate = StringUtils.longestPrefixLookup(uri, prefixMap);

        if(candidate != null && uri.startsWith(candidate.getKey())) {
            String candidateNs = candidate.getKey();
            String candidatePrefix = candidate.getValue();

            int splitIdx = candidateNs.length();

            prefix = candidatePrefix;
            name = uri.substring(splitIdx);
        }
		
        return new String[]{prefix, name};
    }

	public static String prettyUri(String uri, NavigableMap<String, String> prefixMap)
	{
		String[] tmp = decompose(uri, prefixMap);

		String result = (tmp[0].isEmpty())
			? StringUtils.urlDecode(tmp[1])
			: tmp[0] + ":" + StringUtils.urlDecode(tmp[1]);

			return result;
	}
}
