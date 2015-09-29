package controllers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

import models.ActivityModel;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AthleteStatistics{
	String firstName;
	float totalDistance = 0;
	float longestDayDistance = 0;
	int longestDayActivityId;
	String longestDayDate;
	Set<String> years = new LinkedHashSet<String>();
	int daysThisSeason = 0;
	int daysLastSeason = 0;
	int kmThisSeason = 0;
	int kmLastSeason = 0;
	String profile_medium = "/assets/images/avatar.png";
	float maxSpeed = 0;
	int maxSpeedActivityId;
	String maxSpeedDate; 
	
	ZonedDateTime previousActivityDate = ZonedDateTime.now().plusDays(1);
	
	public AthleteStatistics(String name) {
		firstName = name;
	}

	public void collectMaxSpeed(ActivityModel a) {
		if(maxSpeed < a.max_speed) {
			maxSpeed = a.max_speed;
			maxSpeedActivityId = a.id;
			maxSpeedDate = a.start_date;
		}
		
	}

	ObjectNode asJson(){
		ObjectNode json = Json.newObject();
		json.put("firstName", firstName);
		json.put("totalDistance", totalDistance);
		json.put("longestDayActivity", longestDayActivityId);
		json.put("longestDayDistance", longestDayDistance);
		json.put("longestDayDate", longestDayDate);
		json.put("skiedYearsHistory", years.size());
		json.put("skiedDaysThisSeason", daysThisSeason);
		json.put("skiedDaysLastSeason", daysLastSeason);
		json.put("skiedKmThisSeason", kmThisSeason);
		json.put("skiedKmLastSeason", kmLastSeason);
		json.put("profile_medium", profile_medium);
		json.put("maxSpeed", maxSpeed);
		json.put("maxSpeedActivity", maxSpeedActivityId);
		json.put("maxSpeedDate", maxSpeedDate);
		
		
		return json;
		
	}

	public void setProfileAvatar(String profile_medium2) {
		profile_medium = profile_medium2;
		
	}

	public void collectLongestDay(ActivityModel a) {
		if(longestDayDistance < a.distance){
			longestDayDistance = a.distance;
			longestDayActivityId = a.id;
			longestDayDate = a.start_date;
		}
	}

	public void addToTotalDistance(float distance) {
		totalDistance += distance;
		
	}

	public void collectYearsData(ActivityModel a) {
		years.add(Integer.toString(
		ZonedDateTime.parse(a.start_date,
				DateTimeFormatter.ISO_DATE_TIME).getYear()));
		
	}

	public void collectSeasonData(ActivityModel a) {
		if(a.getStartDateAsDate().getDayOfMonth() != previousActivityDate.getDayOfMonth()){
			if(a.getStartDateAsDate().getYear() == ZonedDateTime.now().getYear()){
				daysThisSeason++;
				kmThisSeason += a.distance;
			}

			if(a.getStartDateAsDate().getYear() == ZonedDateTime.now().getYear() - 1){
				daysLastSeason++;
				kmLastSeason += a.distance;
			}
		}
		previousActivityDate = a.getStartDateAsDate();
	}
}