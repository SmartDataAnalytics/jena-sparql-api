package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.vocabulary.XSD;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RDFDatatypeXml
	extends BaseDatatype
{
    protected DocumentBuilder documentBuilder;

    
    public static DocumentBuilder createDefaultDocumentBuilder() {
    	DocumentBuilder result;
		try {
//			result = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setValidating(false);
	        factory.setNamespaceAware(false);

	        result = factory.newDocumentBuilder();
	        result.setEntityResolver(new EntityResolver() {

	            @Override
	            public InputSource resolveEntity(String publicId, String systemId)
	                    throws SAXException, IOException {
	                //System.out.println("Ignoring " + publicId + ", " + systemId);
	                return new InputSource(new StringReader(""));
	            }
	        });
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}

    	return result;
    }
    
    public RDFDatatypeXml() {
        this(createDefaultDocumentBuilder());
    }
    
    public RDFDatatypeXml(DocumentBuilder documentBuilder) {
        this(XSD.getURI() + "xml", documentBuilder);
    }

    public RDFDatatypeXml(String uri, DocumentBuilder documentBuilder) {
        super(uri);
    	this.documentBuilder = documentBuilder;
    }

//    public RDFDatatypeXml(String uri, Gson gson) {
//        super(uri);
//        this.gson = gson;
//    }

    @Override
    public Class<?> getJavaClass() {
        return Node.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
    	Node node = (Node)value;
    	String result = toString(node);
    	return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public synchronized Node parse(String lexicalForm) throws DatatypeFormatException {
        Document result;
		try {
			result = documentBuilder.parse(new InputSource(new StringReader(lexicalForm)));
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}

        return result;
    }
    
    
    public static String toString(Node node)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            toText(node, baos);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }

        String result = baos.toString();
        return result;
    }

//    public static Document createFromString(String text)
//        throws Exception
//    {
//        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse
//            (new InputSource(new StringReader(text)));
//    }

    public static void toText(Node node, OutputStream out)
        throws TransformerFactoryConfigurationError, TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source source = new DOMSource(node);
        Result output = new StreamResult(out);
        transformer.transform(source, output);
    }    
}
