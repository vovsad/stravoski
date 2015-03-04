package models;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;

import com.fasterxml.jackson.databind.JsonNode;

@Entity
public class Activities {
	public static JsonNode activities;
	
	public static List<JsonNode> skiActivitiesAsList(){
		if (activities == null) return new LinkedList();
		
		LinkedList<JsonNode> list = new LinkedList();
		for (Iterator<JsonNode> i = activities.iterator(); i.hasNext();) {
			JsonNode node = i.next();
			if(node.findValues("type").toString().contains("AlpineSki"))
				list.add(node);
		}
		return list;
	}

}
