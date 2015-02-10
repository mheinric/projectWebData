package org.gpsgeneration;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.gpsgeneration.eventTrace.EventTrace;
import org.gpsgeneration.gpx.GpxType;

public class Main {

	static String inputFile ;
	static String dataFolder ;
	static String outFile ;
	
	static Properties config = new Properties() ;

	/**
	 * Entry point of the application
	 * @param args
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws DatatypeConfigurationException 
	 */
	public static void main(String[] args) {
		parseArgs(args) ;

		EventTrace input = EventTraceRead.read(inputFile) ;
		
		SimpleRouting sr = new SimpleRouting(dataFolder) ;
		sr.preprocess(input) ;
		GpxType res = sr.processInput(input) ;
		GpxIO.write(res) ;
	}

	public static void parseArgs(String[] args){
		int i = 0 ;
		while(i < args.length)
		{
			switch(args[i])
			{
			case "-o" :
				if(i == args.length -1)
				{
					System.err.println("Option -o expects an other argument") ;
					printUsage() ;
					System.exit(1) ;
				}
				outFile = args[i+1] ;
				i += 2 ;
				break ;

			default :
				if(dataFolder == null)
					dataFolder = args[i] ;
				else
				{
					if(inputFile == null)
						inputFile = args[i] ;
					else 
					{
						System.err.println("Unexpected argument : " + args[i]) ;
						printUsage() ;
						System.exit(1) ;
					}
				}
				i++ ;
				break ;
			}
		}
		if(inputFile == null)
		{
			System.err.println("Missing argument") ;
			printUsage() ;
		}
		if(outFile == null)
			outFile = "out.gpx" ;
	}


	public static void printUsage(){
		System.err.println("Usage : java -jar Gpsgeneration.jar [-o <output file>] <data folder> <input file>") ;
		System.err.println() ;
		System.err.println("<data folder> : the name of the folder that contains the data " +
				"used for the genration.") ;
		System.err.println("This folder must contain a map.osm.pbf file containing the maps " +
				"data (from OpenStreetMap for example)") ;
		System.err.println("<input file> : the name of the input xml file, containing " +
				"the sequence of events") ;
		System.err.println("<output file> : (optional) the name of the output file") ;
	}

	public static void loadConfig(){
		try {
			config.load(new FileInputStream(dataFolder + "/config.cfg")) ;
		} catch (IOException e) {
			System.err.println("Could not load config file " + e.getMessage()) ;
		}
		String timeStep = config.getProperty("timestep", "60") ;
		OutputGPSTrace.pointTime = Integer.parseInt(timeStep) ;
	}
}
