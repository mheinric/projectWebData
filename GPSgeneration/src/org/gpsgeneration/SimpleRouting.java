package org.gpsgeneration;


import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

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
	
	private Path folderPath ;
	private GraphHopper gh = new GraphHopper() ;
	
	public SimpleRouting(String folderName) {
		String userDir = System.getProperty("user.dir") ;
		folderPath = Paths.get(userDir) ;
		folderPath = folderPath.resolve(folderName) ;
		importGraph(gh, "BIKE,FOOT," + TrainFlagEncoder.NAME) ;
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
	
	public void doRouting(String inFile, String outFile)
		throws FileNotFoundException, JAXBException, DatatypeConfigurationException{
		
		GHPlace startPlace = new GHPlace(48.2, 2.5) ;
		//GHPlace endPlace = new GHPlace(48.5, 2.6) ;
	
		locatePlace st = new locatePlace();
		OSMNode endPlace = st.find(folderPath.toString(), 48.5, 2.6);
		System.out.println("EndPlace: Lat: " + endPlace.getLat() + " | Lon: " + endPlace.getLon()) ;
		
		GHPlace endplace = new GHPlace( endPlace.getLat(), endPlace.getLon());
		
		GHRequest request = new GHRequest(startPlace,  endplace).setVehicle(CustomCarEncoder.NAME) ;

		GHResponse response = gh.route(request) ;
		
		PointList l = response.getPoints() ;
		if(response.hasErrors())
			response.getErrors().get(0).printStackTrace() ;
		System.out.println("route: DONE") ;
		List<SimpleGpxPoint> sl = OutputGPSTrace.getPoints(gh, l, 
				DatatypeFactory.newInstance().newXMLGregorianCalendar(2015,2,5,18,0,0,0,0), 
				TrainFlagEncoder.NAME);
		System.out.println("getPoints: DONE") ;

		GpxIO.write(OutputGPSTrace.convert(sl), outFile);
	}

}