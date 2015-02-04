# projectWebData

##Presentation 
This project aims at the generation of fake GPS data. This project contains two major ideas:
- First, being able to generate consecutive positions of an imaginary person given a certain timeline of events. This part uses road maps to generate the consecutive positions. 

- Second, given a certain GPS trace, being able to determine wether it is fake or not. This part relies on statistical analysis of GPS trace, and also on the use of road maps to decide wether it comes from real measurments, or not.


##Generation de données GPS

### Compiling
To compile this part of the project, the requirements are :
- [Apache Ant](http://ant.apache.org/)
- [Java JDK 1.5 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (Tested with version 1.7)
- Some JAR libraries must also be dowloaded manually in order to compile this project (see after)

The following libraries must be put in the folder GPSgeneration/lib:
 - [Graphhopper](https://github.com/graphhopper/graphhopper) for the routing part of the application
 - [Traveling salesman] (http://sourceforge.net/projects/travelingsales/) for some utilities to work with osm data files
  
The following jar libraries must be put in the folder GPSgeneration/ant-lib:
 - [JABX](https://jaxb.java.net/) XML library, to convert .xsd files into java files
 
Once this is done, the project can be compiled by typing **ant** into the folder GPSgeneration. 

The produced jar file will be created in the folder GPSgeneration/jar.


##TODO 
- [x] setup GitHub.
- [ ] Add XML Schemas (or DTD) for the datatypes used.
- [ ] Write very simple functionnal programs
- [ ] Import road data from either OpenStreetMap or GoogleMaps
- [ ] (Optional) Take into account traffic informations, traffic lights, data from counting measurments...


##Usage 
TODO
