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

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
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

		return redirect("/index");
	}

	private Thread syncStravaToDB() {
		final JStravaV3 strava = new JStravaV3(session("Access_token"));
		final Athlete athlete = strava.getCurrentAthlete();

		Thread cacheThread = new Thread("GetCachedActivities") {
			public void run() {
				Logger.debug("Thread: " + getName() + " running");

				if (DBController.cachedActivitiesCount() == 0 ||
						!isActivitiesSyncked()) {

					cacheAthlete();
					cacheNewestAthleteActivities();
				}
			}

			private void cacheAthlete() {
				AthleteModel athleteDB = Ebean.find(AthleteModel.class,
						athlete.getId());

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

				Long before = ZonedDateTime.parse(DBController.getMinActivityDate(),
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
			
			private Boolean isActivitiesSyncked() {
				Logger.debug("Here we are");
				Long after = ZonedDateTime.parse(DBController.getMaxActivityDate(),
						DateTimeFormatter.ISO_DATE_TIME).toEpochSecond();
				List<Activity> activities = strava
						.getCurrentAthleteActivitiesAfterDate(after);

				return activities.isEmpty();
			}
		};
		
		cacheThread.start();
		
		return cacheThread;
	}

	private Thread updateDownhillDistanceToDB() {
		final JStravaV3 strava = new JStravaV3(session("Access_token"));

		Thread updateThread = new Thread("updateDownhillDistance") {
			public void run() {
				for (ActivityModel a: DBController.getSkiActivities()){
					if(a.downhill_distance == 0){
						a.setDownhill_distance(getDownhillDistance(a.id));
						a.update();
					}
				}
			}
			
		private int getDownhillDistance(int Id){
			
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
		};
	updateThread.start();
	
	return updateThread;
	}

	public Result login() {
		return redirect("https://www.strava.com/oauth/authorize?client_id=1455&redirect_uri=http://localhost:9000/tokenexchange&response_type=code");
	}

	public Result getActivities() {
		try {
			syncStravaToDB().join();
		} catch (InterruptedException e) {
			return status(1, "Cannot cache data from Strava");
		}
		return ok(Json.toJson(DBController.getSkiActivities()));
	}
	
	public Result getActivitiesSynced(){
		try {
			syncStravaToDB().join();
		} catch (InterruptedException e) {
			return status(1, "Cannot cache data from Strava");
		}
		return ok();
	}
	
	public Result getDownhillDistanceUpdated(){
		try {
			updateDownhillDistanceToDB().join();
		} catch (InterruptedException e) {
			return status(1, "Cannot cache data from Strava");
		}
		return ok();
	}
	
	//TODO: refactor me please
	public Result getAthleteStatistics() {
		List<ActivityModel> activities = DBController.getSkiActivities();
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
