package org.gpsgeneration;

import java.io.File;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.gpsgeneration.eventTrace.EventTrace;
import org.xml.sax.SAXException;

public class EventTraceRead {
	
	public static final String EVT_PATH="org.gpsgeneration.eventTrace" ;
	private static String EVT_SCHEMA_FILE = "schemas/gpx.xsd" ;
	private static Schema EVT_SCHEMA ;
	private static JAXBContext jcontext ; 
	
	static {
		SchemaFactory fact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI) ;
		URL url = ClassLoader.getSystemResource(EVT_SCHEMA_FILE) ;
		try {
			EVT_SCHEMA = fact.newSchema(url) ;
		} catch (SAXException e) {
			e.printStackTrace() ;
			System.exit(1) ;
		}
	}
	
	static {
		try {
			jcontext = JAXBContext.newInstance(EVT_PATH);
		} catch (JAXBException e) {
			e.printStackTrace();
			System.exit(1) ;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static EventTrace read(String fileName) {
		try{
		File f = new File(fileName) ;
		Unmarshaller um = jcontext.createUnmarshaller() ;
		um.setSchema(EVT_SCHEMA) ;
		return  ((JAXBElement<EventTrace>) um.unmarshal(f)).getValue() ;
		} catch (JAXBException e) {
			System.out.println("Error occured while reading input : " + e.getMessage()) ;
			System.exit(1) ;
			return null ;
		}
	}

}
