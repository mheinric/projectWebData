package org.gpsgeneration;

import java.util.GregorianCalendar;

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
	
	/**
	 * Date in milliseconds
	 */
	public long date ;
	
	public SimpleGpxPoint(double lat, double lon, long date) {
		super(lat, lon) ;
		this.date = date ;
	}
	
	public SimpleGpxPoint(double lat, double lon, XMLGregorianCalendar date) {
		super(lat, lon) ;
		this.date = date.toGregorianCalendar().getTimeInMillis() ;
	}
	
	public XMLGregorianCalendar getXMLDate(){
		GregorianCalendar calendar = new GregorianCalendar() ;
		calendar.setTimeInMillis(date) ;
		return fact.newXMLGregorianCalendar(calendar) ;
	}
	
	/**
	 * Converts from the calendar to time in ms
	 * @param c
	 * @return
	 */
	public static long getTime(XMLGregorianCalendar c){
		return c.toGregorianCalendar().getTimeInMillis() ;
	}

}
