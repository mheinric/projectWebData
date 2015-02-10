package org.gpsgeneration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.graphhopper.reader.OSMElement;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.pbf.PbfReader;
import com.graphhopper.reader.pbf.Sink;
import com.graphhopper.util.shapes.GHPlace;

public class locatePlace {

	//private GHPlace res = new GHPlace(0, 0) ;;
	private double latc = 0;
	private double lonc = 0;
	private OSMNode min = new OSMNode(0,0,0);

	public OSMNode find(String folderName, double lat, double lon){
		latc = lat;
		lonc = lon;

		Sink s = new Sink() {
			private int i = 0 ;
			double mlat = 0;
			double mlon = 0;
			double md = 10000;

			double elat = 0;
			double elon = 0;
			double ed = 0;
			@Override
			public void process(OSMElement e) {
				if(((e.getTag("amenity") != null) || (e.getTag("building") != null)) && (e instanceof OSMNode))
				{
					OSMNode enode = (OSMNode) e;
					elat = enode.getLat();
					elon = enode.getLon();
					ed = (elat - latc)*(elat - latc) + (elon - lonc)*(elon - lonc);
					if (ed < md)
					{
						mlat = elat; 
						mlon = elon; 
						md = ed; 
						min = (OSMNode) e;
					};

					i++ ;
				}

			}

			@Override
			public void complete() {
				System.out.println("Total : " + i) ;
			}
		};

		PbfReader reader;
		try {
			reader = new PbfReader(new FileInputStream(folderName +"/map.osm.pbf"), s, 1);
			reader.run() ;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return min;
	}
}
