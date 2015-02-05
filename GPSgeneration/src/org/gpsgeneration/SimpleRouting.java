package org.gpsgeneration;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.gpsgeneration.gpx.GpxType;
import org.gpsgeneration.gpx.TrkType;
import org.gpsgeneration.gpx.TrksegType;
import org.gpsgeneration.gpx.WptType;
import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.Selector;
import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPlace;
import com.graphhopper.reader.DataReader;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPoint;




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
		importGraph(gh, "CAR,BIKE,FOOT") ;
	}
	
	private void importGraph(GraphHopper g, String mode){
		g.disableCHShortcuts();
		g.setInMemory(true) ;
		g.setOSMFile(folderPath.resolve("map.osm.pbf").toString()) ;
		g.setGraphHopperLocation("/home/christophe/Bureau/projectWebData/GPSgeneration/data/map");
		g.setEncodingManager(new EncodingManager(mode));
		g.importOrLoad() ;
	}
	
	public void doRouting(String inFile, String outFile)
		throws FileNotFoundException, JAXBException, DatatypeConfigurationException{
		
		GHPlace startPlace = new GHPlace(48.2, 2.5) ;
		GHPlace endPlace = new GHPlace(48.5, 2.6) ;
		GHRequest request = new GHRequest(startPlace, endPlace).setVehicle("CAR") ;
		GHResponse response = gh.route(request) ;
		
		PointList l = response.getPoints() ;
		GpxType trace = new GpxType() ;
		TrkType trk = new TrkType() ;
		TrksegType trkseg = new TrksegType() ;
		
		trace.getTrk().add(trk) ;
		trk.getTrkseg().add(trkseg) ;
		for(int i = 0 ; i < l.getSize() ; i ++)
		{
			WptType wp = new WptType() ;
			wp.setLat(new BigDecimal(l.getLat(i))) ;
			wp.setLon(new BigDecimal(l.getLon(i))) ;
			XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(2015, 1, 1, 12 + i /60, i %60, 0, 0, 0) ;
			wp.setTime(date) ;
			trkseg.getTrkpt().add(wp) ;
		}
		GpxIO.write(trace, outFile);
	}

}