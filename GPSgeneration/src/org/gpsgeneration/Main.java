package org.gpsgeneration;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

public class Main {

	/**
	 * Entry point of the application
	 * @param args
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws JAXBException, URISyntaxException, IOException {
		GpxIO.write(GpxIO.read("traces/test.gpx")) ;
	}

}
