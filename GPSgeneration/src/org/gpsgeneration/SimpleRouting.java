package org.gpsgeneration;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.gpsgeneration.eventTrace.EventTrace;
import org.gpsgeneration.eventTrace.MoveAction;
import org.gpsgeneration.eventTrace.TransportType;
import org.gpsgeneration.gpx.GpxType;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPlace;


/**
 * Contains static methods to import a graph, and perform routing on this graph
 * @author heinrich
 *
 */
public class SimpleRouting {
	
	private static DatatypeFactory fact ;
	static {
		try {
			fact = DatatypeFactory.newInstance() ;
		} catch (DatatypeConfigurationException e) {
			System.out.println("Fatal error : " + e.getMessage()) ;
			System.exit(1) ;
		}
	}
	
	private Path folderPath ;
	private GraphHopper gh = new GraphHopper() ;
	
	public SimpleRouting(String folderName) {
		String userDir = System.getProperty("user.dir") ;
		folderPath = Paths.get(userDir) ;
		folderPath = folderPath.resolve(folderName) ;
		importGraph(gh, "FOOT," + CustomCarEncoder.NAME + "," + TrainFlagEncoder.NAME) ;
	}
	
	private void importGraph(GraphHopper g, String mode){
		g.disableCHShortcuts();
		g.setInMemory(true) ;
		g.setOSMFile(folderPath.resolve("map.osm.pbf").toString()) ;
		g.setGraphHopperLocation("temp");
		g.setEncodingManager(new EncodingManager(mode));
		g.importOrLoad() ;
		System.out.println("import: DONE") ;
	}
	
	public GpxType processInput(EventTrace trace) {
		for(MoveAction action : trace.getMoveAction())
		{
			List<SimpleGpxPoint> p = processEvent(action) ;
		}
		
		return null ;
	}
	
	private List<SimpleGpxPoint> processEvent(MoveAction action) {
		double startLat = action.getFromLocation().getLat() ;
		double startLon = action.getFromLocation().getLon() ;
		double endLat = action.getToLocation().getLat();
		double endLon = action.getToLocation().getLon();
		
		PointList l = doRouting(startLat, startLon, endLat, endLon) ;
		return OutputGPSTrace.getPoints(gh, l, action.getStartTime(), getMode(action.getTransport())) ;
	}
	
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
			System.out.println("Unexpected Transport mode " + transport) ;
			return CustomCarEncoder.NAME ;
		}
		
	}
	
	
	public PointList doRouting(double startLat, double startLon, double endLat, double endLon) {
		
		GHPlace startPlace = new GHPlace(startLat, startLon) ;
	
		//locatePlace st = new locatePlace();
		//OSMNode endPlace = st.find(folderPath.toString(), 48.5, 2.6);
		//System.out.println("EndPlace: Lat: " + endPlace.getLat() + " | Lon: " + endPlace.getLon()) ;
		
		GHPlace endplace = new GHPlace( endLat, endLon);
		
		GHRequest request = new GHRequest(startPlace,  endplace).setVehicle(CustomCarEncoder.NAME) ;

		GHResponse response = gh.route(request) ;
		
		PointList l = response.getPoints() ;
		if(response.hasErrors())
			response.getErrors().get(0).printStackTrace() ;
		return l ;
	}

}