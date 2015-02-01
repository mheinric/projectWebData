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

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.CmdArgs;
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
		importGraph() ;
	}
	
	private void importGraph(){
		gh = new GraphHopper() ;
		gh.setInMemory(true) ;
		gh.setOSMFile(folderPath.resolve("map.osm.pbf").toString()) ;
		gh.init(new CmdArgs()) ;
		gh.setMemoryMapped() ;
		gh.importOrLoad() ;
	}
	
	public void doRouting(String inFile, String outFile) 
			throws FileNotFoundException, JAXBException, DatatypeConfigurationException{
		
		
		GHPlace startPlace = new GHPlace(48.2, 2.5) ;
		GHPlace endPlace = new GHPlace(48.5, 2.6) ;
		GHRequest request = new GHRequest(startPlace, endPlace) ;
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
		
		GpxIO.write(trace, outFile) ;
	}
	
	
	/*
	public static void setUp(String folderName){
		String userDir = System.getProperty("user.dir") ;
		Path folderPath = Paths.get(userDir) ;
		folderPath = folderPath.resolve(folderName) ;
		
		
		
		
	}*/

	/*
	public static GraphHopper importGraph(String fileName) throws IOException {
		Enumeration<URL> ressourceURL = ClassLoader.getSystemResources(fileName) ;
		if(!ressourceURL.hasMoreElements())
			throw new FileNotFoundException(fileName) ;
		URL url = ressourceURL.nextElement() ;
		GraphHopper gh = new GraphHopper() ;
		gh.setInMemory(true) ;
		gh.setOSMFile(url.getFile()) ;
		gh.init(new CmdArgs()) ;
		//gh.setMemoryMapped() ;
		gh.importOrLoad() ;
		return gh ;
	}*/
	
}
