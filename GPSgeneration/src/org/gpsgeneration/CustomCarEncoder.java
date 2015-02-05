package org.gpsgeneration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import com.graphhopper.routing.util.CarFlagEncoder;

/**
 * Uses custom speed for the different types of roads. 
 * 
 * @author heinrich
 *
 */
public class CustomCarEncoder extends CarFlagEncoder {
	
	public static final String NAME = "custom-car:org.gpsgeneration.CustomCarEncoder" ;

	private static final String DEFAULT_FILE = "defaults/maxspeed.properties" ;

	private static final Properties DEFAULT_PROPERTIES = new Properties() ;

	static {
		try {
			DEFAULT_PROPERTIES.load(ClassLoader.getSystemResourceAsStream(DEFAULT_FILE)) ;
		} catch (IOException e) {
			e.printStackTrace() ;
			System.exit(1) ;
		}
	}

	public CustomCarEncoder(){
		super(5,5) ;
		String speedFile = Main.dataFolder + "/maxspeed.properties" ;

		Properties prop ;
		try {
			FileInputStream in = new FileInputStream(speedFile) ;
			prop = new Properties(DEFAULT_PROPERTIES) ;
			prop.load(in) ;


		} catch(IOException e) {
			System.err.println("Could not load maxspeed file " + e.getMessage()) ;
			System.err.println("Falling back to defaults") ;
			prop = DEFAULT_PROPERTIES ;
		}
		for(Entry<Object, Object> entry : prop.entrySet())
		{
			defaultSpeedMap.put((String) entry.getKey(), 
					Integer.parseInt((String) entry.getValue())) ;
		}
	}

	@Override
	public String toString(){
		return NAME ;
	}
	
}
