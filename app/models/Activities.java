package models;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Entity
public class Activities {
	public static JsonNode activities;
	
	public static List<JsonNode> skiActivitiesAsList(){
		if (activities == null) return new LinkedList<JsonNode>();
		
		LinkedList<JsonNode> list = new LinkedList<JsonNode>();
		for (Iterator<JsonNode> i = activities.iterator(); i.hasNext();) {
			JsonNode node = i.next();
			if(node.findValue("type").asText().contains("AlpineSki"))
				list.add(node);
		}
		return list;
	}
	
    public static ObjectNode getStatistics(){
    	
    	if (activities == null) return Json.newObject();
    	ObjectNode statistics = Json.newObject();
    	int count = 0, totalDistance = 0;
		for (Iterator<JsonNode> i = activities.iterator(); i.hasNext();) {
			JsonNode node = i.next();
			if(node.findValue("type").asText().contains("AlpineSki")){
				count++;
				totalDistance += node.findValue("distance").asDouble();
			}
				
		}
		statistics.put("Count", count);
		statistics.put("Total Distance", totalDistance);
		return statistics;
    }


}
