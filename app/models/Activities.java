package models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import play.Logger;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Entity
public class Activities {
	public static List<JsonNode> activities;
	public static String season = Seasons.season1415;

	public static JsonNode filterToSkiActivities() {
		
		ArrayNode skiActivities = JsonNodeFactory.instance.arrayNode();

		((List<JsonNode>) Cache.get("Activities")).forEach((activity) -> {
			if (activity.findValue("type").asText().contains("AlpineSki")) {
				skiActivities.add(activity);
			}
		});

		return skiActivities;

	}
	
	public static JsonNode filterToSelectedDates() {
		
		ArrayNode filteredActivities = JsonNodeFactory.instance.arrayNode(); 
				
		filterToSkiActivities().forEach((activity) -> {
			if (getTo(season) > getActivityDate(activity) && 
					getActivityDate(activity) > getFrom(season)){
				filteredActivities.add(activity);
			}
		});

		return filteredActivities;

	}

	public static ObjectNode getStatistics() {

		ObjectNode statistics = Json.newObject();

		statistics.put("count", getSkiActivitiesCount());
		statistics.put("total_skiing_days", getSkiActivitiesDays());
		statistics.put("total_distance", getSkiSeasonTotalDistanceInKm());
		statistics.put("max_skiing_day", getSkiSeasonLongestRideInKm());

		return statistics;
	}

	public static int getSkiActivitiesDays() {

		List<String> dates = new LinkedList<String>();
		for (Iterator<JsonNode> i = filterToSelectedDates().iterator(); i.hasNext();) {
			JsonNode node = i.next();
			dates.add(node.findValue("start_date").asText().substring(0, 10));
		}

		return dates.parallelStream().distinct().collect(Collectors.toList())
				.size();
	}

	public static int getSkiActivitiesCount() {
		return filterToSelectedDates().size();
	}

	public static int getSkiSeasonTotalDistanceInKm() {
		int totalDistance = 0;
		for (Iterator<JsonNode> i = filterToSelectedDates().iterator(); i.hasNext();) 
				totalDistance += i.next().findValue("distance").asDouble();


		return totalDistance / 1000;
	}

	public static int getSkiSeasonLongestRideInKm() {
		int maxDistance = 0;
		for (Iterator<JsonNode> i = filterToSelectedDates().iterator(); i.hasNext();){
			JsonNode node = i.next();
			maxDistance = maxDistance > node.findValue("distance")
			.asDouble() ? maxDistance : node.findValue("distance")
			.asInt();
		}
		
		return maxDistance / 1000;
	}
	
	private static Long getFrom(String season) {
		final DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		try {
			return format.parse(season.split("-")[0]).getTime() / 1000;
		} catch (ParseException e) {
			Logger.error("Cannot parse date from season " + season);
			return null;
		}

	}

	private static Long getTo(String season) {
		final DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		try {
			return format.parse(season.split("-")[1]).getTime() / 1000;
		} catch (ParseException e) {
			Logger.error("Cannot parse date from season " + season);
			return null;
		}

	}
	
	private static Long getActivityDate(JsonNode activity) {
		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			return format.parse(
						activity.findValue("start_date").asText()
						.substring(0, 10)).getTime() / 1000;
		} catch (ParseException e) {
			Logger.error("Cannot parse date from Strava date " + 
					activity.findValue("start_date").asText());
			return null;
		}

	}

}
