package org.gpsgeneration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.gpsgeneration.gpx.GpxType;
import org.gpsgeneration.gpx.TrkType;
import org.gpsgeneration.gpx.TrksegType;
import org.gpsgeneration.gpx.WptType;
import org.openstreetmap.osm.data.coordinates.LatLon;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PointList;

/**
 * Functions used to produce a real GPS trace from just a sequence of nodes
 * that this trace will go through.
 * We use linear interpolation between the different nodes, and use speed limits data 
 * to generate coherent data
 * @author heinrich
 *
 */
public class OutputGPSTrace {
	
	/**
	 * Radius of earth, in kms
	 */
	public static final double EARTH_RADIUS = 6371 ;
	
	/**
	 * Number of seconds between any two measurements
	 */
	public static int pointTime = 60 ;
	
	/**
	 * Produces a series of GPxPoints from a series of points that were obtained by a query.  
	 * @param gh
	 * @param l
	 * @param date
	 * @return
	 * @throws DatatypeConfigurationException 
	 */
	public static List<SimpleGpxPoint> getPoints(GraphHopper gh, PointList l, 
			XMLGregorianCalendar date, String mode) 
			throws DatatypeConfigurationException {
		assert(l.size() > 0) ;
		List<SimpleGpxPoint> outl = new ArrayList<>() ;
		FlagEncoder e  = gh.getEncodingManager().getEncoder(mode) ;
	
		LocationIndex index = gh.getLocationIndex() ;
		DatatypeFactory fact = DatatypeFactory.newInstance() ;
		outl.add(new SimpleGpxPoint(l.getLat(0), l.getLon(0), (XMLGregorianCalendar) date.clone())) ;
		
		//Last point got through
		LatLon last = new LatLon(outl.get(0).lat, outl.get(0).lon) ;
		//Time elapsed since last measurment
		double elapsedTime = 0 ;
		//Total time since beginning
		double totalTime = 0 ;
		int i = 1 ;
		while(i < l.getSize())
		{
			QueryResult res = index.findClosest(last.lat(), last.lon(), EdgeFilter.ALL_EDGES) ;
			double speed = e.getSpeed(res.getClosestEdge().getFlags()) ;
			speed = Math.max(speed, 5) ;
			double nextLat = l.getLat(i) ;
			double nextLon = l.getLon(i) ;
			double distance = distance(last.lat(), last.lon(), nextLat, nextLon) ;
			double segTime = distance / speed * 3600 ;
			while(elapsedTime + segTime > pointTime)
			{
				double diffLat = (nextLat - last.lat()) /distance *speed * (pointTime-elapsedTime)/3600 ;
				double diffLon = (nextLon - last.lon()) /distance *speed * (pointTime-elapsedTime)/3600 ;
				XMLGregorianCalendar newDate = (XMLGregorianCalendar) date.clone();
				newDate.add(fact.newDuration((int) ((totalTime + pointTime - elapsedTime)*1000))) ;
				
				SimpleGpxPoint p = new SimpleGpxPoint(last.lat() + diffLat, last.lon() + diffLon, newDate) ;
				outl.add(p) ;
				elapsedTime = 0 ;
				last = new LatLon(p.lat, p.lon);
				distance = distance(last.lat(), last.lon(), nextLat, nextLon) ;
				segTime = distance / speed * 3600 ;
				totalTime += pointTime ;
			}
			elapsedTime += segTime ;
			totalTime += segTime ;
			last = new LatLon(nextLat, nextLon) ;
			i++ ;
		}
		XMLGregorianCalendar newDate = (XMLGregorianCalendar) date.clone();
		newDate.add(fact.newDuration((int) ((totalTime + pointTime - elapsedTime)*1000))) ;
		SimpleGpxPoint p = new SimpleGpxPoint(last.lat(), last.lon(), newDate) ;
		outl.add(p) ;

		return outl ;
	}
	
	public static GpxType convert(List<SimpleGpxPoint> l) {
		GpxType gpx = new GpxType() ;
		TrkType trk = new TrkType() ;
		TrksegType trkseg = new TrksegType() ;
		for(SimpleGpxPoint p : l)
			trkseg.getTrkpt().add(convert(p)) ;
		trk.getTrkseg().add(trkseg) ;
		gpx.getTrk().add(trk) ;
		return gpx ;
	}
	
	public static WptType convert(SimpleGpxPoint p) {
		
		WptType pt = new WptType() ;
		pt.setLat(new BigDecimal(p.lat)) ;
		pt.setLon(new BigDecimal(p.lon)) ;
		pt.setTime(p.date) ;
		
		return pt ;
	}
	
	/**
	 * Computes the distance in kms between two points
	 * @param fromLat
	 * @param fromLon
	 * @param toLat
	 * @param toLon
	 * @return
	 */
	public static double distance(double fromLat, double fromLon, double toLat, double toLon ){
		LatLon l1 = new LatLon(fromLat, fromLon) ;
		LatLon l2 = new LatLon(toLat, toLon) ;
		return ((double)LatLon.distanceInMeters(l1, l2))/1000 ;	
		}

}
