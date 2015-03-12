package models;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Entity
public class Activities {
	public static JsonNode activities;

	private static JsonNode filterToSkiActivities() {
		if (activities == null)
			return null;
		ArrayNode skiActivities = JsonNodeFactory.instance.arrayNode();

		activities.forEach((activity) -> {
			if (activity.findValue("type").asText().contains("AlpineSki")) {
				skiActivities.add(activity);
			}
		});

		return skiActivities;

	}

	public static List<JsonNode> skiActivitiesAsList() {
		if (activities == null)
			return new LinkedList<JsonNode>();

		LinkedList<JsonNode> onlySkiActivities = new LinkedList<JsonNode>();
		for (Iterator<JsonNode> i = filterToSkiActivities().iterator(); i
				.hasNext();)
			onlySkiActivities.add(i.next());

		return onlySkiActivities;
	}

	public static ObjectNode getStatistics() {

		if (activities == null)
			return Json.newObject();
		ObjectNode statistics = Json.newObject();

		statistics.put("Count", getSkiActivitiesCount());
		statistics.put("Total Skiing Days", getSkiActivitiesDays());
		statistics.put("Total Distance", getSkiSeasonTotalDistanceInKm());
		statistics.put("Max Skiing Day", getSkiSeasonLongestRideInKm());

		return statistics;
	}

	private static int getSkiActivitiesDays() {
		if (activities == null)
			return 0;

		List<String> dates = new LinkedList<String>();
		for (Iterator<JsonNode> i = filterToSkiActivities().iterator(); i.hasNext();) {
			JsonNode node = i.next();
			dates.add(node.findValue("start_date").asText().substring(0, 10));
		}

		return dates.parallelStream().distinct().collect(Collectors.toList())
				.size();
	}

	private static int getSkiActivitiesCount() {
		if (activities == null)
			return 0;

		return filterToSkiActivities().size();
	}

	private static int getSkiSeasonTotalDistanceInKm() {
		if (activities == null)
			return 0;

		int totalDistance = 0;
		for (Iterator<JsonNode> i = filterToSkiActivities().iterator(); i.hasNext();) 
				totalDistance += i.next().findValue("distance").asDouble();


		return totalDistance / 1000;
	}

	private static int getSkiSeasonLongestRideInKm() {
		if (activities == null)
			return 0;

		int maxDistance = 0;
		for (Iterator<JsonNode> i = filterToSkiActivities().iterator(); i.hasNext();){
			JsonNode node = i.next();
			maxDistance = maxDistance > node.findValue("distance")
			.asDouble() ? maxDistance : node.findValue("distance")
			.asInt();
		}
		
		return maxDistance / 1000;
	}

}
