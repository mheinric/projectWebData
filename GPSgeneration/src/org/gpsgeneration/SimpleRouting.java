package org.gpsgeneration;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.datatype.XMLGregorianCalendar;

import org.gpsgeneration.eventTrace.EventTrace;
import org.gpsgeneration.eventTrace.Location;
import org.gpsgeneration.eventTrace.MoveAction;
import org.gpsgeneration.eventTrace.TransportType;
import org.gpsgeneration.gpx.GpxType;
import org.openstreetmap.osm.data.coordinates.LatLon;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPlace;


/**
 * Contains static methods to import a graph, and perform routing on this graph
 * @author heinrich
 *
 */
public class SimpleRouting {

	private LocatePlace locate = new LocatePlace(Main.dataFolder) ;

	private Path folderPath ;
	private GraphHopper gh = new GraphHopper() ;

	private Map<Object, LatLon> idToLocation = new HashMap<>() ;

	private Location lastLocation = null ;
	
	Random rand = new Random() ;

	public SimpleRouting(String folderName) {
		String userDir = System.getProperty("user.dir") ;
		folderPath = Paths.get(userDir) ;
		folderPath = folderPath.resolve(folderName) ;
		importGraph(gh, "FOOT," + CustomCarEncoder.NAME + "," + TrainFlagEncoder.NAME) ;
	}

	/**
	 * Imports the graph
	 * @param g
	 * @param mode
	 */
	private void importGraph(GraphHopper g, String mode){
		g.disableCHShortcuts();
		g.setInMemory(true) ;
		g.setOSMFile(folderPath.resolve("map.osm.pbf").toString()) ;
		g.setGraphHopperLocation("temp");
		g.setEncodingManager(new EncodingManager(mode));
		g.importOrLoad() ;
		System.out.println("import: DONE") ;
	}

	public void preprocess(EventTrace trace) {
		for(MoveAction action : trace.getMoveAction())
		{
			preprocess(action.getFromLocation()) ;
			preprocess(action.getToLocation()) ;
		}
		locate.parse() ;
	}

	private void preprocess(Location loc){
		if(loc == null)
			return ;
		if(loc.getRefId() == null && loc.getPrecision() != null)
			locate.addQuery(loc.getLat(), loc.getLon(), loc.getPrecision()) ;
	}

	/**
	 * processes the input file
	 * @param trace
	 * @return
	 */
	public GpxType processInput(EventTrace trace) {
		List<SimpleGpxPoint> result = new ArrayList<>() ;
		for(MoveAction action : trace.getMoveAction())
		{
			List<SimpleGpxPoint> p = processEvent(action) ;

			if(! result.isEmpty())
			{
				result.addAll(OutputGPSTrace.filling(
						result.get(result.size()-1).date, p.get(0).date, 
						p.get(0).lat, p.get(0).lon)) ;
			}
			result.addAll(p) ;
		}
		return OutputGPSTrace.convert(result) ;
	}

	/**
	 * Process one single event, and outputs the corresponding points
	 * @param action
	 * @return
	 */
	private List<SimpleGpxPoint> processEvent(MoveAction action) {

		LatLon start = processLocation(action.getFromLocation()) ;
		LatLon end = processLocation(action.getToLocation()) ;
		
		
		lastLocation = action.getToLocation() ;
		
		String tmode = getMode(action.getTransport()) ;

		XMLGregorianCalendar startTime = action.getStartTime() ;
		XMLGregorianCalendar endTime = action.getEndTime() ;

		PointList l = doRouting(start.lat(), start.lon(), end.lat(), end.lon()) ;
		if(startTime != null)
		{
			long sTime = SimpleGpxPoint.getTime(startTime) ;
			if(endTime != null)
			{
				long eTime = SimpleGpxPoint.getTime(endTime) ;

				List<SimpleGpxPoint> lp = OutputGPSTrace.getPoints(gh, l, 
						startTime, tmode) ;
				long realEnd = lp.get(lp.size() -1).date ; 
				if(realEnd - eTime > 0 )
				{
					double ratio = ((long) eTime - sTime) / (realEnd - sTime)  ;
					for(SimpleGpxPoint p : lp)
						p.date = (long) (sTime + ratio * (p.date - sTime)) ;
				}
				return lp ;
			}
			else
				return OutputGPSTrace.getPoints(gh, l, startTime, tmode) ;
		}
		else
		{
			if(endTime != null)
			{
				long eTime = SimpleGpxPoint.getTime(endTime) ;
				List<SimpleGpxPoint> lp = OutputGPSTrace.getPoints(gh, l, 
						endTime, tmode) ;
				long realEnd = lp.get(lp.size() - 1).date ;
				long diff = realEnd - eTime ;
				for(SimpleGpxPoint p : lp)
					p.date -= diff ;
				return lp ;
			}
			else
			{
				throw new RuntimeException("Error in input file, you must " +
						"provide either startTime or endTime") ;
			}
		}
	}

	/**
	 * Given a location, returns the corresponding coordinates.
	 * Handles IDs, and precision in the requests
	 * @param loc
	 * @return
	 */
	public LatLon processLocation(Location loc){
		if(loc == null)
			return new LatLon(lastLocation.getLat(), lastLocation.getLon()) ;
		if(loc.getRefId() != null)
		{
			Location refLoc = (Location) loc.getRefId() ;
			
			if(idToLocation.containsKey(refLoc.getId()))
				return idToLocation.get(refLoc.getId()) ;
			else
				throw new RuntimeException("Id " + loc.getRefId() 
						+ " is referenced, but its value is not defined before") ;
						
		}

		LatLon ll ;
		if(loc.getPrecision() == null)
			ll = new LatLon(loc.getLat(), loc.getLon()) ;
		else 
		{
			ll = locate.getResult(loc.getLat(), loc.getLon(), loc.getPrecision()) ;
		}
		if(ll == null) 
		{
			ll = new LatLon(loc.getLat() + (rand.nextDouble()*2 -1)*loc.getPrecision(),
					loc.getLon() + (rand.nextDouble()*2 -1)*loc.getPrecision()) ;
		}
		if(loc.getId() != null)
			idToLocation.put(loc.getId(), ll) ;
		return ll ;
	}

	/**
	 * Returns the transport mode used to query the map
	 * @param transport
	 * @return
	 */
	public String getMode(TransportType transport)
	{
		switch(transport)
		{
		case CAR :
			return CustomCarEncoder.NAME ;
		case FOOT :
			return "foot" ;
		case TRAIN :
			return TrainFlagEncoder.NAME ;
		default :
			System.out.println("Warning : Unexpected Transport mode " + transport) ;
			return CustomCarEncoder.NAME ;
		}

	}

	/**
	 * Gets the path between two points
	 */
	public PointList doRouting(double startLat, double startLon, double endLat, double endLon) {

		GHPlace startPlace = new GHPlace(startLat, startLon) ;
		GHPlace endplace = new GHPlace( endLat, endLon);

		GHRequest request = new GHRequest(startPlace,  endplace).setVehicle(CustomCarEncoder.NAME) ;
		GHResponse response = gh.route(request) ;

		PointList l = response.getPoints() ;
		if(response.hasErrors())
		{
			response.getErrors().get(0).printStackTrace() ;
			System.exit(1) ;
		}
		return l ;
	}
}