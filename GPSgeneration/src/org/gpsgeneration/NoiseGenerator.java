package org.gpsgeneration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.gpsgeneration.gpx.GpxType;
import org.gpsgeneration.gpx.TrkType;
import org.gpsgeneration.gpx.TrksegType;
import org.gpsgeneration.gpx.WptType;

public class NoiseGenerator {
	
	public double noiseParam = 0.01 ;
	
	List<GpxType> gpxTraces = new ArrayList<>() ;
	
	/**
	 * Infers the noise parameter from a bunch of static traces.
	 * @param folderPath
	 */
	public void inferParam(String folderPath) {
		File folder = new File(folderPath) ;
		for(File f : folder.listFiles())
		{
			if(!f.getName().endsWith(".gpx"))
				continue ;
			GpxType t = GpxIO.read(f) ;
			gpxTraces.add(t) ;
		}
		int i = 0 ;
		double sigmaSquare = 0 ;
		for(GpxType trace : gpxTraces)
		{
			for(TrkType trk : trace.getTrk())
			{
				for(TrksegType seg : trk.getTrkseg())
				{
					double meanLon = 0 ;
					double meanLat = 0 ;
					for(WptType wp : seg.getTrkpt())
					{
						meanLat += wp.getLat().doubleValue() ;
						meanLon += wp.getLon().doubleValue() ;
					}
					meanLat /= seg.getTrkpt().size() ;
					meanLon /= seg.getTrkpt().size() ;
					
					for(WptType wp : seg.getTrkpt())
					{
						double lat = wp.getLat().doubleValue() ;
						double lon = wp.getLon().doubleValue() ;
						sigmaSquare += (lat - meanLat) * (lat - meanLat) ;
						sigmaSquare += (lon - meanLon) * (lon - meanLon) ;
						i += 2 ;
					}
				}
			}
		}
		sigmaSquare /= i ;
		noiseParam = Math.sqrt(sigmaSquare) ;
		System.out.println("Inferred noise parameter : " + noiseParam) ;
	}
	
	public void addNoise(List<SimpleGpxPoint> l) {
		Random rand = new Random() ;
		for(SimpleGpxPoint p : l)
		{
			p.lat += rand.nextGaussian() * noiseParam ;
			p.lon += rand.nextGaussian() * noiseParam ;
		}
	}

}
