package org.gpsgeneration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.graphhopper.reader.OSMReader;
import com.graphhopper.reader.OSMRelation;
import com.graphhopper.reader.OSMTurnRelation.TurnCostTableEntry;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.OSMTurnRelation;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodedValue;

public class TrainFlagEncoder extends CarFlagEncoder {
	
	public static final String NAME ="train:org.gpsgeneration.TrainFlagEncoder" ;
	
	private Set<String> acceptedRailways = new HashSet<>() ;
	
	private Map<String, Integer> defaultSpeed = new HashMap<>() ;
	private EncodedValue relationCodeEncoder ;
	private double walkSpeed = 5 ;
	
	public TrainFlagEncoder() {
		super(5,5) ;
		
		acceptedRailways.add("funicular") ;
		acceptedRailways.add("light_rail") ;
		acceptedRailways.add("monorail") ;
		//acceptedRailways.add("rail") ;
		acceptedRailways.add("subway") ;
		acceptedRailways.add("tram") ;
		
		defaultSpeed.put("funicular", 10) ;
		defaultSpeed.put("light_rail", 10) ;
		defaultSpeed.put("monorail", 50) ;
		//defaultSpeed.put("rail", 150) ;
		defaultSpeed.put("subway", 60) ;
		defaultSpeed.put("tram", 20) ;
		
	}
	
	@Override
	public int defineRelationBits(int index, int shift){
		relationCodeEncoder = new EncodedValue("RelationCode", shift, 1, 1, 0, 1);
		return shift + relationCodeEncoder.getBits() ;
	}
	
	@Override
	public long acceptWay(OSMWay way) {
		String trainTag = way.getTag("railway") ;
		if(trainTag != null && acceptedRailways.contains(trainTag))
			return acceptBit ;
		else 
			return 0 ;
	}
	
	@Override
	public String toString(){
		return NAME ;
	}
	
	@Override
	public long handleRelationTags(OSMRelation relation, long oldRelationTag){
		if(relation.hasTag("railway", "station") 
				|| relation.hasTag("public_transport", "platform", 
						"stop_position", "stop_area", "station"))
		{
			return relationCodeEncoder.setValue(0, 1) ;
		}
		return oldRelationTag ;
	}
	
	@Override
	public long handleWayTags(OSMWay way, long allowed, long relationCode){
		if((allowed & acceptBit) == 0 && relationCodeEncoder.getValue(relationCode) != 0)
		{
			return setSpeed(0, walkSpeed) ;
		}
		
		if((allowed & acceptBit) == 0)
			return 0 ;
		long encoded = 0 ;
		
		double speed = getSpeed(way) ;
		encoded = setSpeed(0, speed) ;
		encoded |= directionBitMask ;
		
		return encoded ;
	}
	
	@Override
	public long handleNodeTags(OSMNode node){
		return 0;
	}
	
	@Override
	protected double getSpeed(OSMWay way){
		return defaultSpeed.get(way.getTag("railway")) ;
	}
	
	public double getSpeed(long flags){
		if(relationCodeEncoder.getValue(flags) != 0)
			return walkSpeed ;
		else 
			return super.getSpeed(flags) ;
	}
	
	public Collection<TurnCostTableEntry> analyzeTurnRelation(OSMTurnRelation rel, OSMReader reader) {
		return Collections.emptyList() ;
	}

}
