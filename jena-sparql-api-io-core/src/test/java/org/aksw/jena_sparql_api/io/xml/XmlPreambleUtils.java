package org.aksw.jena_sparql_api.io.xml;

import java.io.FileReader;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.jena.riot.RDFDataMgr;

public class XmlPreambleUtils {
	public static void main(String[] args) throws XMLStreamException {

		InputStream in = RDFDataMgr.open("service-status.rdf");
		
		// Instance of the class which helps on reading tags
		XMLInputFactory factory = XMLInputFactory.newInstance();

		// Initializing the handler to access the tags in the XML file
		XMLEventReader eventReader = factory.createXMLEventReader(in);

		
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		XMLEventWriter eventWriter = outFactory.createXMLEventWriter(System.out);
		
		// Checking the availability of the next tag
		while (eventReader.hasNext()) {			
			XMLEvent xmlEvent = eventReader.nextEvent();

			if(xmlEvent.isStartDocument()) {
				eventWriter.add(xmlEvent);
				eventWriter.add(XMLEventFactory.newFactory().createCharacters("\n"));
			}
			
			if (xmlEvent.isStartElement()) {
				StartElement startElement = xmlEvent.asStartElement();

				//System.out.println("Got element: " + startElement);
				eventWriter.add(startElement);

				// Read all attributes when start tag is being read
				@SuppressWarnings("unchecked")
				Iterator<Attribute> iterator = startElement.getAttributes();

				while (iterator.hasNext()) {
					Attribute attribute = iterator.next();					
					eventWriter.add(attribute);
					// QName name = attribute.getName();

					//System.out.println("Got attribute: " + name);
				}
				
				XMLEvent closeEvent = eventReader.nextEvent();
				eventWriter.add(closeEvent);

				
				eventWriter.flush();
				if(true) {
					System.out.println("foo");
					return;
				}
			}

		}

	}
}
