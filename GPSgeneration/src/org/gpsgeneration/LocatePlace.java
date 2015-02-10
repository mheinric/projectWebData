package org.gpsgeneration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.coordinates.Bounds;
import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import com.graphhopper.reader.OSMElement;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.pbf.PbfReader;
import com.graphhopper.reader.pbf.Sink;

/**
 * Object used to find particular places.
 * We first enter some queries, then ask it to parse the map file, 
 * and finally we can retrieve the results
 */
public class LocatePlace {

	/**
	 * Precision for the queries. Two points closer than that will be mapped to 
	 * the same query.
	 */
	private static final double PRECISION = 0.001 ;
	
	private Random rand = new Random() ;
	private String folder ;
	
	public LocatePlace(String folder){
		this.folder = folder ;
	}
	
	/**
	 * Map for the queries. To each query associates the number of times 
	 * that it was requested. This avoid having a lot of queries 
	 * all at the same place
	 */
	Map<LatLon, Integer> queries = new HashMap<>() ;
	
	/**
	 * The precision of each of the queries
	 */
	Map<LatLon, Double> precisions = new HashMap<>() ;
	
	/**
	 * Maps to a given position the nodes that were found.
	 */
	Map<LatLon, List<OSMNode>> found = new HashMap<>() ;
	
	/**
	 * The nodes that were selected to keep in memory.
	 * Presented as a dataSet.
	 */
	IDataSet results = new MemoryDataSet() ;
	
	/**
	 * Number of nodes added to the results
	 */
	private int nbNode = 0 ;
	
	
	public void addQuery(double lat, double lon, double prec) {
		for(Map.Entry<LatLon, Integer> entry : queries.entrySet())
		{
			LatLon pos = entry.getKey() ;
			double ratio = precisions.get(pos)/prec ; 
			if(pos.lat() > lat - PRECISION 
					&& pos.lat() < lat + PRECISION 
					&& pos.lon() > lon - PRECISION
					&& pos.lon() < lon + PRECISION
					&& ratio > 0.25 && ratio < 4)
			{
				entry.setValue(entry.getValue()+ 1) ;
				precisions.put(pos, Math.min(precisions.get(pos), prec)) ;
				return ; 
			}
		}
		LatLon pos = new LatLon(lat, lon) ; 
		queries.put(pos, 1) ;
		precisions.put(pos, prec) ;
	}
	
	public LatLon getResult(double lat, double lon, double prec) {
		Node res = null ;
		int i = 1 ;
		Iterator<Node> it = results.getNodes(new Bounds(new LatLon(lat, lon), prec)) ;
		while(it.hasNext())
		{
			Node n = it.next() ;
			if(rand.nextInt(i) == 0)
			{
				res = n ;
			}
			i++ ;
		}
		if(res == null)
			return null ;
		else 
			return new LatLon(res.getLatitude(), res.getLongitude()) ;
	}
	
	
	public void parse() {
		Sink s = new Sink() {
			@Override
			public void process(OSMElement e) {
				if((e instanceof OSMNode) && e.hasTag("amenity"))
				{
					OSMNode enode = (OSMNode) e;
					double elat = enode.getLat();
					double elon = enode.getLon();
					for(Map.Entry<LatLon, Integer> entry : queries.entrySet())
					{
						double prec = precisions.get(entry.getKey()) ;
						double qlat = entry.getKey().lat() ;
						double qlon = entry.getKey().lon() ;
						if(elat > qlat - prec && elat < qlat + prec
								&& elon > qlon - prec && elon < qlon + prec)
						{
							results.addNode(convertToNode(enode)) ;
							break ;
						}
					}
				}

			}

			@Override
			public void complete() {
				System.out.println("Finished parsing file, results contain " + nbNode + " nodes.") ;
			}
		};

		PbfReader reader;
		try {
			reader = new PbfReader(new FileInputStream(folder +"/map.osm.pbf"), s, 1);
			reader.run() ;
		} catch (FileNotFoundException e1) {
			System.out.println("Map file not found " + e1.getMessage()) ;
			System.exit(1) ;
		}
	}
	
	public Node convertToNode(OSMNode n) {
		List<Tag> l = new LinkedList<>() ;
		if(n.hasTag("building"))
			l.add(new Tag("building", n.getTag("building")));
		if(n.hasTag("amenity"))
			l.add(new Tag("amenity", n.getTag("amenity"))) ;
		Node newNode = new Node(nbNode, 0, (Date) null, (OsmUser) null, 
				0, l, n.getLat(), n.getLon()) ;
		nbNode ++ ;
		return newNode ;
	}
}
