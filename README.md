# Generation of GPS data

## Presentation 
This project aims at the generation of fake GPS data. That is, we want to generate points at fixed time intervals that would look like the successive position of a real person. To achieve this, the work was devided into two with an other group in the following way:
- Generate a series of event that describe the movement with a high level description (done by the other group)
- From this high level description, generate the successive positions (done by our group)

The data format used to communicate between the two parts is describe in the XML schema in the file GpsGeneration/data/schemas/eventTrace.xsd. This data is composed of a series of events. Each event specifies a startDate and/or an endDate, a destination location, optionnally a source location, and a mode of transportation.
Each location is given as two coordinates, and eventually a precision. This precision is used to take realistic positions for the destinations of the target (for example, avoid having a house in the middle of a field, or an airport).

The program we developp uses data from OpenStreetMap to generate these traces. It also allows to set some parameters (eg. timestep between measurments, amount of noise, speed limits...).

The program works as follows :
- A first pass on the input determines some locations that must be searched on the map. Using these locations, some data is extracted from the OpenStreetMap file to set an exact location to all the events.
- During the second pass, each event is processed, and for each, a path is computed, and then transformed into a series of successive positions.

## Limitations
We wanted to integrate into the model either traffic data (like car counts, or reports of accidents) or weather data to influence the speed at which a person should move. However, we did not really found ways to get these data easily. Also some technical problems made it more difficult than expected to extract usable information out of the map data. Things like adding a way to get from one point to an other using trains or subway proved to be difficult, for some minor reasons like how to transfer from one line to an other. So even if an important part of technical work was already available in the libraries we used, adapting it for our purpose was more difficult than expected.

## Program

### Compiling
To compile this part of the project, the requirements are :
- [Apache Ant](http://ant.apache.org/)
- [Java JDK 1.7 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (Tested with version 1.7)
- Some JAR libraries must also be dowloaded manually in order to compile this project (see after)

The following libraries must be put in the folder GPSgeneration/lib:
 - [Graphhopper v0.3](https://graphhopper.com/) for the routing part of the application
 - [Traveling salesman] (http://sourceforge.net/projects/travelingsales/) for some utilities to work with osm data files

The following jar libraries must be put in the folder GPSgeneration/ant-lib:
 - [JABX](https://jaxb.java.net/) XML library, to convert .xsd files into java files
 
Once this is done, the project can be compiled by typing **ant** into the folder GPSgeneration.

The executable jar file will be created in the folder GPSgeneration/jar.

Type **ant clean** to remove all the generated files, and **ant doc** to generate the code documentation.

###Usage 
Once it is compiled, the program can be run with the following command :
```
java -jar jar/GpsGeneration.jar [-o <out-file>] [-t <temp-file>] <path-to-data> <path-to-input>
```

Where we have :
- &lt path-to-data &gt is the path to a folder that contains : 
	- a map file named map.osm.pbf
	- optionnally a config.cfg and a maxspeed.properties file see GpsGeneration/example folder for examples
	
- <path-to-input> gives the location of the input event trace as an XML file.
- <out-file> is the name of the file that will be produced
- <temp-file> is the name of a temporary folder that will be used to store a compact representation of the graph. Once it is created, further queries to the same graph will be faster. However, you should not give the same name for two different map data. By default the temporary files will be created in folder **temp**.

