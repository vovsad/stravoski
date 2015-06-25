package controllers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import models.ActivityModel;
import models.AthleteModel;

import org.jstrava.authenticator.AuthResponse;
import org.jstrava.authenticator.StravaAuthenticator;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.athlete.Athlete;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.*;
import play.libs.Json;
import play.libs.F.Function;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.*;
import views.html.*;
import views.html.defaultpages.error;

public class Application extends Controller {

	public Result index() {
		return ok(index.render(session("Access_token") != null
				&& !session("Access_token").isEmpty()));
	}

	public Result tokenExchange(String code) {
		Logger.debug("in tokenExchange");

		StravaAuthenticator authenticator = new StravaAuthenticator(1455,
				"http://localhost:9000/tokenexchange",
				"22122cf967940aa0d142f51ca987b878aba948eb");

		AuthResponse authResponse = authenticator.getToken(code);

		session("Access_token", authResponse.getAccess_token());
		syncStravaToDB();

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

	public Result login() {
		return redirect("https://www.strava.com/oauth/authorize?client_id=1455&redirect_uri=http://localhost:9000/tokenexchange&response_type=code");
	}

	public Result getActivities() {
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
	
	public Result getAthleteStatistics() {
		List<ActivityModel> activities = DBController.getSkiActivities();
		
		ObjectNode statistics = Json.newObject();
		
		float totalDistance = 0;
		float longestDay = 0;
		String longestDayDate = "";
		
		for (ActivityModel a : activities){
			totalDistance += a.distance;
			if(longestDay < a.distance){
				longestDay = a.distance;
				longestDayDate = a.start_date;
			}
			
		}
		statistics.put("totalDistance", totalDistance/1000);
		statistics.put("longestDay", longestDay/1000);
		statistics.put("longestDayDate", longestDayDate);
		statistics.put("skiedYearsHistory", getSkiSeasonsYears().size());
		
		
		return ok(statistics);
	}
	
	private Set<String> getSkiSeasonsYears(){
		Set<String> years = new LinkedHashSet();  
		
		List<ActivityModel> activities = DBController.getSkiActivities();
		for (ActivityModel a : activities){
			years.add(Integer.toString(
				ZonedDateTime.parse(a.start_date,
					DateTimeFormatter.ISO_DATE_TIME).getYear()));
			
		}
		return years;
	}

}
