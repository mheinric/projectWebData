package org.gpsgeneration;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.graphhopper.util.shapes.GHPoint;

/**
 * Simple class to represent a point in a Gpx trace.
 * @author heinrich
 *
 */
public class SimpleGpxPoint extends GHPoint {
	
	private static DatatypeFactory fact ;
	static {
		try {
			fact = DatatypeFactory.newInstance() ;
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			System.exit(1) ;
		}
	}
	public final XMLGregorianCalendar date ;
	
	public SimpleGpxPoint(double lat, double lon, int year, int month, int day, int hour, int min, int sec) {
		super(lat, lon) ;
		this.date = fact.newXMLGregorianCalendar(year, month, day, hour, min, sec, 0, 0) ;
	}
	
	public SimpleGpxPoint(double lat, double lon, XMLGregorianCalendar date) {
		super(lat, lon) ;
		this.date = date ;
	}

}
