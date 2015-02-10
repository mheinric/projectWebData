package org.gpsgeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.gpsgeneration.gpx.GpxType;
import org.gpsgeneration.gpx.ObjectFactory;
import org.xml.sax.SAXException;

/**
 * Class that provides utility methods to write and read GpxType object 
 * to and from their XML representation.
 * @author heinrich
 *
 */
public class GpxIO {

	private static final ObjectFactory objectFactory = new ObjectFactory() ;
	public static final String GPX_PATH="org.gpsgeneration.gpx" ;
	private static JAXBContext jcontext ; 
	private static String GPX_SCHEMA_FILE = "schemas/gpx.xsd" ;
	private static Schema GPX_SCHEMA ;
	
	static {
		SchemaFactory fact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI) ;
		URL url = ClassLoader.getSystemResource(GPX_SCHEMA_FILE) ;
		try {
			GPX_SCHEMA = fact.newSchema(url) ;
		} catch (SAXException e) {
			e.printStackTrace() ;
			System.exit(1) ;
		}
	}

	static {
		try {
			jcontext = JAXBContext.newInstance(GPX_PATH);
		} catch (JAXBException e) {
			e.printStackTrace();
			System.exit(1) ;
		}
	}


	/**
	 * Writes the GPS trace in the output file
	 * @param trace : the trace to write in XML
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public static void write(GpxType trace) {
		try{
		JAXBElement<GpxType> el = objectFactory.createGpx(trace) ;
		Marshaller marshaller = jcontext.createMarshaller() ;
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(el, new File(Main.outFile)) ;
		} catch (JAXBException e) {
			System.out.println("Error while writing output file : " + e.getMessage()) ;
			System.exit(1) ;
		}
	}

	/**
	 * Reads a GpxType object from its XML representation
	 * @param fileName : the name of the file to read
	 * @return The GpxType object created from the XML file 
	 */
	public static GpxType read(String fileName) {
		File f = new File(fileName) ;
		return read(f) ;
	}
	
	@SuppressWarnings("unchecked")
	public static GpxType read(File file) {
		try {
			Unmarshaller um = jcontext.createUnmarshaller();
			um.setSchema(GPX_SCHEMA) ;
			return ((JAXBElement<GpxType>) um.unmarshal(file)).getValue() ;
		} catch (JAXBException e) {
			System.out.println("Error while parsing file " + e ) ;
			System.exit(1) ;
			return null ;
		}
	}
	
	
	public static URL load(String path) throws IOException {
		Enumeration<URL> ressourceURL = ClassLoader.getSystemResources(path) ;
		if(!ressourceURL.hasMoreElements())
			throw new FileNotFoundException(path) ;
		else
			return ressourceURL.nextElement() ;
	}
	
}
