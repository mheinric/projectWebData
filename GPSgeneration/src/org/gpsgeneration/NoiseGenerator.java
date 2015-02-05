package org.gpsgeneration;

import java.util.List;
import java.util.Random;

public class NoiseGenerator {
	
	private static double noiseParam = 0.01 ;
	
	public static void inferParam(String folder) {
		
	}
	
	public static void addNoise(List<SimpleGpxPoint> l) {
		Random rand = new Random() ;
		for(SimpleGpxPoint p : l)
		{
			p.lat += rand.nextGaussian() * noiseParam ;
			p.lon += rand.nextGaussian() * noiseParam ;
		}
	}

}
