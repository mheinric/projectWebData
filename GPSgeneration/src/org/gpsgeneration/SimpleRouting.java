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

	private LatLon lastLocation = null ;

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
		g.setGraphHopperLocation(Main.tempFolder);
		g.setEncodingManager(new EncodingManager(mode));
		g.importOrLoad() ;
		System.out.println("import: DONE") ;
	}

	public void preprocess(EventTrace trace) {
		for(MoveAction action : trace.getMoveAction())
		{
			if(action.getTransport() == TransportType.TRAIN)
			{
				trainProcess(action.getFromLocation()) ;
				trainProcess(action.getToLocation()) ;
			}
			preprocess(action.getFromLocation()) ;
			preprocess(action.getToLocation()) ;
		}
		locate.parse() ;
	}

	private void trainProcess(Location loc){
		if(loc.getRefId() != null)
			loc = (Location) loc.getRefId() ;
		locate.addStationQuery(loc.getLat(), loc.getLon());
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
	public List<SimpleGpxPoint> processInput(EventTrace trace) {
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
		return result ;
	}

	/**
	 * Process one single event, and outputs the corresponding points
	 * @param action
	 * @return
	 */
	private List<SimpleGpxPoint> processEvent(MoveAction action) {
		
		if(action.getTransport() == TransportType.TRAIN)
			return processTrainEvent(action) ;

		LatLon start = processLocation(action.getFromLocation()) ;
		LatLon end = processLocation(action.getToLocation()) ;

		String tmode = getMode(action.getTransport()) ;

		List<SimpleGpxPoint> ptList = doRouting(start.lat(), start.lon(), end.lat(), end.lon(), tmode) ;
		matchDates(ptList, action) ;
		return ptList ;
	}
	

	private List<SimpleGpxPoint> processTrainEvent(MoveAction action) {

		LatLon sloc = processLocation(action.getFromLocation()) ;
		LatLon eloc = processLocation(action.getToLocation()) ;

		LatLon trainStart = locate.getStation(sloc.lat(), sloc.lon()) ;
		LatLon trainEnd = locate.getStation(eloc.lat(), eloc.lon()) ;

		List<SimpleGpxPoint> l1 = doRouting(sloc.lat(), sloc.lon(), 
				trainStart.lat(), trainStart.lon(), CustomCarEncoder.NAME) ;
		
		List<SimpleGpxPoint> l2 = doRouting(trainStart.lat(), trainStart.lon(),
				trainEnd.lat(), trainEnd.lon(), TrainFlagEncoder.NAME, CustomCarEncoder.NAME) ;
		
		List<SimpleGpxPoint> l3 = doRouting(trainEnd.lat(), trainEnd.lon(), 
				eloc.lat(), eloc.lon(), CustomCarEncoder.NAME) ;

		double diffTime = l1.get(l1.size() -1).date + OutputGPSTrace.pointTime*1000 ;
		for(SimpleGpxPoint pt : l2)
			pt.date += diffTime ;
		diffTime = l2.get(l2.size() -1).date + OutputGPSTrace.pointTime*1000 ;
		for(SimpleGpxPoint pt : l3)
			pt.date += diffTime ;
		
		l1.addAll(l2) ;
		l1.addAll(l3) ;

		matchDates(l1, action) ;
		return l1 ;
	}

	/**
	 * Modifies the dates in the list to match with the contraints in the action.
	 * @param ptList
	 * @param action
	 */
	private void matchDates(List<SimpleGpxPoint> ptList, MoveAction action){
		XMLGregorianCalendar startTime = action.getStartTime() ;
		XMLGregorianCalendar endTime = action.getEndTime() ;

		if(startTime != null)
		{
			long sTime = SimpleGpxPoint.getTime(startTime) ;
			long startDiff = sTime - ptList.get(0).date ;
			for(SimpleGpxPoint pt :ptList)
				pt.date += startDiff ;

			if(endTime == null)
				endTime = action.getMaxEndTime() ;

			if(endTime != null)
			{
				long eTime = SimpleGpxPoint.getTime(endTime) ;


				long realEnd = ptList.get(ptList.size() -1).date ; 
				if(realEnd - eTime > 0 )
				{
					double ratio = ((long) eTime - sTime) / (realEnd - sTime)  ;
					for(SimpleGpxPoint p : ptList)
						p.date = (long) (sTime + ratio * (p.date - sTime)) ;
				}
				return ;
			}
			else
				return ;
		}
		else
		{
			if(endTime != null)
			{
				long eTime = SimpleGpxPoint.getTime(endTime) ;

				long realEnd = ptList.get(ptList.size() - 1).date ;
				long diff = realEnd - eTime ;
				for(SimpleGpxPoint p : ptList)
					p.date -= diff ;

				startTime = action.getMinStartTime() ;
				if(startTime != null)
				{
					long sTime = SimpleGpxPoint.getTime(startTime) ;
					long realStart = ptList.get(0).date ;
					if(realStart < sTime)
					{
						double ratio = ((long) eTime - sTime) / (realEnd - sTime)  ;
						for(SimpleGpxPoint p : ptList)
							p.date = (long) (sTime + ratio * (p.date - sTime)) ;
					}

				}
				return ;
			}
			else
			{
				throw new RuntimeException("Error in input file, you must " +
						"provide either startTime or endTime") ;
			}}
	}

	/**
	 * Given a location, returns the corresponding coordinates.
	 * Handles IDs, and precision in the requests
	 * @param loc
	 * @return
	 */
	public LatLon processLocation(Location loc){
		if(loc == null)
		{
			if(lastLocation == null)
			{
				System.out.println("You must provide at least the first fromLocation") ;
				System.exit(1) ;
			}
			return lastLocation ;
		}
			
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
			ll = locate.getResult(loc.getLat(), loc.getLon(), loc.getPrecision()) ;
		
		if(ll == null) 
		{
			ll = new LatLon(loc.getLat() + (rand.nextDouble()*2 -1)*loc.getPrecision(),
					loc.getLon() + (rand.nextDouble()*2 -1)*loc.getPrecision()) ;
		}
		if(loc.getId() != null)
			idToLocation.put(loc.getId(), ll) ;
		lastLocation = ll ;
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
			return TrainFlagEncoder.NAME +",foot" ;
		default :
			System.out.println("Warning : Unexpected Transport mode " + transport) ;
			return CustomCarEncoder.NAME ;
		}

	}

	/**
	 * Gets the path between two points, tries all the modes in this order
	 */
	public List<SimpleGpxPoint> doRouting(double startLat, double startLon, double endLat, double endLon, 
			String mode, String... otherModes) {

		GHPlace startPlace = new GHPlace(startLat, startLon) ;
		GHPlace endplace = new GHPlace( endLat, endLon);

		GHRequest request = new GHRequest(startPlace,  endplace).setVehicle(mode) ;
		GHResponse response = gh.route(request) ;

		PointList l = response.getPoints() ;
		if(response.hasErrors())
		{
			System.out.println("Error while routing " + response.getErrors().get(0)) ;
		}
		double speed = 50 ;
		switch (mode) {
		case CustomCarEncoder.NAME:
			speed = 60 ;
			break;

		case "foot":
			speed = 5 ;
			break;
		case TrainFlagEncoder.NAME:
			speed = 100 ;
			break ;
		}
		
		if(!response.isFound())
		{
			if(otherModes.length == 0)
			{
				System.out.println("No path found, going into straight line") ;
				return OutputGPSTrace.staightLine(startLat, startLon, endLat, endLon, speed) ;
			}
			else
			{
				String nextMode = otherModes[0] ;
				System.out.println("Not found for " + mode + " trying with " + mode) ;
				String[] otherModes2 = new String[otherModes.length -1] ;
				for(int i = 0 ; i < otherModes2.length ; i++)
					otherModes2[i] = otherModes[i+1] ;
				return doRouting(startLat, startLon, endLat, endLon, nextMode, otherModes2) ;
			}
		}
		
		return OutputGPSTrace.getPoints(gh, l, 0, mode) ;
	}
}