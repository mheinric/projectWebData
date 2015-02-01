package org.gpsgeneration;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;



public class Main {
	
	static String inputFile ;
	static String dataFolder ;
	static String outFile ;

	/**
	 * Entry point of the application
	 * @param args
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws DatatypeConfigurationException 
	 */
	public static void main(String[] args) throws JAXBException, URISyntaxException, IOException, DatatypeConfigurationException {
		SimpleRouting sr = new SimpleRouting(dataFolder) ;
		sr.doRouting(inputFile, outFile) ;
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
						System.exit(1) ;
 					}
				}
				break ;
			}
		}
		if(outFile == null)
			outFile = "out.gpx" ;
	}

}
