package controllers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import models.ActivityModel;
import models.AthleteModel;
//import controllers.DBController;


import org.jstrava.authenticator.AuthResponse;
import org.jstrava.authenticator.StravaAuthenticator;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.athlete.Athlete;
import org.jstrava.entities.stream.Stream;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {
	
	public Result index() {
		return ok(views.html.index.render(session("Access_token") != null
				&& !session("Access_token").isEmpty()));
	}

	public Result tokenExchange(String code) {
		Logger.debug("in tokenExchange");

		StravaAuthenticator authenticator = new StravaAuthenticator(1455,
				"http://localhost:9000/tokenexchange",
				"22122cf967940aa0d142f51ca987b878aba948eb");

		AuthResponse authResponse = authenticator.getToken(code);

		session("Access_token", authResponse.getAccess_token());
		session("Athlete_id", Long.toString(authResponse.getAthlete().getId()));

		return redirect("/index");
	}

	private boolean syncStravaToDB() {
		
		if (!isLastActivitiesLoaded()) {

			cacheAthlete();
			cacheNewestAthleteActivities();
		}
		
		return true;
	}

	private void cacheAthlete() {
		final JStravaV3 strava = new JStravaV3(session("Access_token"));
		final Athlete athlete = strava.getCurrentAthlete();

		
		AthleteModel athleteDB = Ebean.find(AthleteModel.class, athlete.getId());

		if (athleteDB == null) {
			athleteDB = new AthleteModel(athlete);
			athleteDB.save();
		} else if (!athleteDB.updated_at
				.equals(athlete.getUpdated_at())) {
			athleteDB = new AthleteModel(athlete);
			athleteDB.update();
		}
	}

	private void cacheNewestAthleteActivities() {
		final JStravaV3 strava = new JStravaV3(session("Access_token"));
		
		for (Activity a : strava.getCurrentAthleteActivities()) {
			Logger.debug(a.toString());

			ActivityModel activityDB = Ebean.find(ActivityModel.class,
					a.getId());

			if (activityDB == null) {
				activityDB = new ActivityModel(a);
				activityDB.save();
			}

		}

		cacheOldestAthleteActivities();

	}

	private void cacheOldestAthleteActivities() {
		final JStravaV3 strava = new JStravaV3(session("Access_token"));

		Long before = ZonedDateTime.parse(DBController.getMinActivityDate(session("Athlete_id")),
				DateTimeFormatter.ISO_DATE_TIME).toEpochSecond();
		List<Activity> activities = strava
				.getCurrentAthleteActivitiesBeforeDate(before);

		if (!activities.isEmpty()) {
			for (Activity a : activities) {
				Logger.debug(a.toString());

				ActivityModel activityDB = Ebean.find(
						ActivityModel.class, a.getId());

				if (activityDB == null) {
					activityDB = new ActivityModel(a);
					activityDB.save();
				}

			}

			cacheOldestAthleteActivities();
		}

	}
	
	public Result isDataSynced(){
		if(session("Access_token") == null 
				|| session("Access_token").isEmpty()) return unauthorized("{\"unauthorized\": true}");

		return ok("{\"isDataSynced\": " + isLastActivitiesLoaded() + "}");
	}
	
	private Boolean isLastActivitiesLoaded() {
		
		if(DBController.cachedActivitiesCount(session("Athlete_id")) == 0) return false;
		
		final JStravaV3 strava = new JStravaV3(session("Access_token"));
		
		Long after = ZonedDateTime.parse(DBController.getMaxActivityDate(session("Athlete_id")),
				DateTimeFormatter.ISO_DATE_TIME).toEpochSecond();
		List<Activity> activities = strava
				.getCurrentAthleteActivitiesAfterDate(after);

		return activities.isEmpty();
	}

	public Result getDownhillDistanceUpdated() {
		Logger.debug("Calculate Ski without lifts");

		final JStravaV3 strava = new JStravaV3(session("Access_token"));
		Boolean isAnythingUpdated = false;

		for (ActivityModel a: DBController.getSkiActivities(
								Long.toString(strava.getCurrentAthlete().getId()))){
			if(a.downhill_distance == 0){
				a.setDownhill_distance(getDownhillDistance(a.id));
				a.update();
				isAnythingUpdated = true;
			}
		}
		
		return ok("{\"isAnythingUpdated\":" + isAnythingUpdated + "}");
		
		}
			
	private int getDownhillDistance(int Id){
		final JStravaV3 strava = new JStravaV3(session("Access_token"));
		
		Logger.debug("Calculating downhill distance for " + Long.toString(Id));
		
		final String[] types = {"distance", "altitude"}; 
		final List<Stream> streams= strava.findActivityStreams(Id, types);

		List<SimpleEntry<Double, Double>> streamsDataOriginal = new LinkedList<>();
		Iterator<Object> distance = streams.get(1).getData().iterator();
		Iterator<Object> altitude = streams.get(0).getData().iterator();
		for (; altitude.hasNext() && distance.hasNext();) {
			streamsDataOriginal.add(new AbstractMap.SimpleEntry<>((Double)distance
					.next(), (Double)altitude.next()));
		}

		Double previousAltitudePoint = 0.0, previousDistancePoint = 0.0, downhillDistance = 0.0;
		for (SimpleEntry<Double, Double> i : streamsDataOriginal) {

			if (i.getKey() <= previousAltitudePoint) {
				downhillDistance += i.getValue() - previousDistancePoint;
			}
			previousAltitudePoint = i.getKey();
			previousDistancePoint = i.getValue();

		}
		return downhillDistance.intValue();
		
	}
	

	public Result login() {
		return redirect("https://www.strava.com/oauth/authorize?client_id=1455&redirect_uri=http://localhost:9000/tokenexchange&response_type=code");
	}
	
	public Result logout(){
		session("Access_token", "");
		session("Athlete_id", "");
		return redirect("/index");
	}

	public Result getActivities() {
		if(session("Access_token") == null 
				|| session("Access_token").isEmpty()) return unauthorized("{\"unauthorized\": true}");
		
		if(isLastActivitiesLoaded()){
			return ok(Json.toJson(DBController.getSkiActivities(session("Athlete_id"))));
		}else{
			return ok("{\"NoActivities\": true}");
		}
	}
	
	public Result getActivitiesSynced(){
		syncStravaToDB();
		return ok();
	}
	
	
	//TODO: refactor me please
	public Result getAthleteStatistics() {
		List<ActivityModel> activities = DBController.getSkiActivities(session("Athlete_id"));
		ObjectNode statistics = Json.newObject();
		
		if(activities.isEmpty()){
			statistics.put("isempty", true);
			return ok(statistics);
		}
		
		float totalDistance = 0;
		float longestDay = 0;
		ActivityModel longestDayActivity = new ActivityModel();
		String longestDayDate = "";
		Set<String> years = new LinkedHashSet<String>();
		int daysThisSeason = 0;
		int daysLastSeason = 0;
		int kmThisSeason = 0;
		int kmLastSeason = 0;

		ZonedDateTime notTheSameDay = ZonedDateTime.now().plusDays(1);

		
		for (ActivityModel a : activities){
			totalDistance += a.distance;
			
			if(longestDay < a.distance){
				longestDay = a.distance;
				longestDayDate = a.start_date;
				longestDayActivity = a;
			}
			
			years.add(Integer.toString(
					ZonedDateTime.parse(a.start_date,
						DateTimeFormatter.ISO_DATE_TIME).getYear()));
			
			if(a.start_date_asdate.getYear() == ZonedDateTime.now().getYear() &&
					a.start_date_asdate.getDayOfMonth() != notTheSameDay.getDayOfMonth()){
				daysThisSeason++;
				kmThisSeason += a.distance;
			}

			if(a.start_date_asdate.getYear() == ZonedDateTime.now().getYear() - 1 &&
					a.start_date_asdate.getDayOfMonth() != notTheSameDay.getDayOfMonth()){
				daysLastSeason++;
				kmLastSeason += a.distance;
			}
			
			notTheSameDay = a.start_date_asdate;
			
		}
		statistics.put("totalDistance", totalDistance/1000);
		statistics.put("longestDay", longestDay/1000);
		statistics.putPOJO("longestDayActivity", Json.toJson(longestDayActivity));
		statistics.put("longestDayDate", longestDayDate);
		statistics.put("skiedYearsHistory", years.size());
		statistics.put("skiedDaysThisSeason", daysThisSeason);
		statistics.put("skiedDaysLastSeason", daysLastSeason);
		statistics.put("skiedKmThisSeason", kmThisSeason);
		statistics.put("skiedKmLastSeason", kmLastSeason);		
		
		return ok(statistics);
	}
	
}
