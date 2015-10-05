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
import java.util.stream.Collectors;

import javax.inject.Inject;

import models.ActivityModel;
import models.AthleteModel;
import models.StreamsUnnested;

import org.jstrava.authenticator.AuthResponse;
import org.jstrava.authenticator.StravaAuthenticator;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.athlete.Athlete;
import org.jstrava.entities.stream.Stream;

import play.Logger;
import play.Play;
import play.api.mvc.Session;
import play.cache.CacheApi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public class Application extends Controller {
	
	@Inject CacheApi cache;

	
	public Result index() {
		return ok(views.html.index.render(request().cookies().get("AUTH_TOKEN") != null 
				&& request().cookies().get("AUTH_TOKEN").value() != null
				&& !request().cookies().get("AUTH_TOKEN").value().isEmpty()));
	}

	public Result tokenExchange(String code) {
		StravaAuthenticator authenticator = new StravaAuthenticator(1455,
				"http://" + Play.application().configuration().getString("stravoski.host") + "/tokenexchange",
				Play.application().configuration().getString("stravoski.key"));
		
		AuthResponse authResponse = authenticator.getToken(code);

		response().setCookie("AUTH_TOKEN", authResponse.getAccess_token());
		response().setCookie("ATHLETE_ID", Long.toString(authResponse.getAthlete().getId()));

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
		final JStravaV3 strava = new JStravaV3(request().cookies().get("AUTH_TOKEN").value());
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
		final JStravaV3 strava = new JStravaV3(request().cookies().get("AUTH_TOKEN").value());
		
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
		final JStravaV3 strava = new JStravaV3(request().cookies().get("AUTH_TOKEN").value());

		Long before = ZonedDateTime.parse(DBController.getMinActivityDate(request().cookies().get("ATHLETE_ID").value()),
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
		if(request().cookies().get("AUTH_TOKEN") == null 
				|| request().cookies().get("AUTH_TOKEN").value() == null 
				|| request().cookies().get("AUTH_TOKEN").value().isEmpty()) return unauthorized(Json.parse("{\"unauthorized\": true}"));

		return ok(Json.parse("{\"isDataSynced\": " + isLastActivitiesLoaded() + "}"));
	}
	
	private Boolean isLastActivitiesLoaded() {
	
		if(DBController.cachedActivitiesCount(request().cookies().get("ATHLETE_ID").value()) == 0) return false;
		
		final JStravaV3 strava = new JStravaV3(request().cookies().get("AUTH_TOKEN").value());
		
		Long after = ZonedDateTime.parse(DBController.getMaxActivityDate(request().cookies().get("ATHLETE_ID").value()),
				DateTimeFormatter.ISO_DATE_TIME).toEpochSecond();
		List<Activity> activities = strava
				.getCurrentAthleteActivitiesAfterDate(after);

		return activities.isEmpty();
	}

	public Result getActivityCalculated() {
		Logger.debug("Calculate Ski without lifts");

		final JStravaV3 strava = new JStravaV3(request().cookies().get("AUTH_TOKEN").value());
		Boolean isAnythingUpdated = false;

		for (ActivityModel a: DBController.getSkiActivities(
								Long.toString(strava.getCurrentAthlete().getId()))){
			if(a.downhill_distance == 0){
				a.setDownhill_distance(getDownhillDistance(a.id));
				a.update();
				isAnythingUpdated = true;
			}
			if(a.average_downhill_grade == 0){
				a.setAverage_downhill_grade(getAverageGrade(a.id));
				a.update();
				isAnythingUpdated = true;
			}
		}
		
		return ok(Json.parse("{\"isAnythingUpdated\":" + isAnythingUpdated + "}"));
		
		}
			
	public int getDownhillDistance(int id){
		Logger.debug("Calculating downhill distance for " + Long.toString(id));
		
		Double previousAltitudePoint = 0.0, previousDistancePoint = 0.0, downhillDistance = 0.0;
		Table<Double, Double, Double> unnested = getStreamsAsTable(getStream(id));
		
		for (Cell<Double, Double, Double> cell: unnested.cellSet()){
			if(cell.getColumnKey() <= previousAltitudePoint){
				downhillDistance += cell.getRowKey() - previousDistancePoint;
			}
			previousAltitudePoint = cell.getColumnKey();
			previousDistancePoint = cell.getRowKey();
		}
		
		return downhillDistance.intValue();
		
	}
	
	public int getAverageGrade(int id){
		Logger.debug("Detecting slopes for " + Long.toString(id));
		
		Table<Double, Double, Double> unnested = getStreamsAsTable(getStream(id));
		Double previousAltitudePoint = 0.0;
		int grade = 0, count = 0;
		for (Cell<Double, Double, Double> cell: unnested.cellSet()) {

			if (cell.getColumnKey() <= previousAltitudePoint) {
				grade += -cell.getValue();
				count++;
			}
			previousAltitudePoint = cell.getColumnKey();
			

		}
		return Math.round(grade/count);
	}

	public List<Stream> getStream(int id) {
		return cache.getOrElse("stream" + id, () -> {
			final JStravaV3 strava = new JStravaV3(request().cookies().get("AUTH_TOKEN").value());
			final String[] types = {"distance", "altitude", "grade_smooth"}; 
			final List<Stream> streamsAsStravaTracks = strava.findActivityStreams(id, types);

			return streamsAsStravaTracks;
		});
	}
	
	private Table<Double, Double, Double> getStreamsAsTable(List<Stream> rawStreams){
		
		StreamsUnnested streams = new StreamsUnnested();
		
		for (Stream s: rawStreams){
			streams.setData(s);
		}

		return streams.getMergedStreams();
	}

	public Result login() {
		String host = Play.application().configuration().getString("stravoski.host");
		return redirect("https://www.strava.com/oauth/authorize?client_id=1455&redirect_uri=http://" + host + "/tokenexchange&response_type=code");
	}
	
	public Result logout(){
		response().discardCookie("AUTH_TOKEN");
		response().discardCookie("ATHLETE_ID");
		return redirect("/index");
	}

	public Result getActivities() {
		if(request().cookies().get("AUTH_TOKEN") != null 
				&& request().cookies().get("AUTH_TOKEN").value() == null 
				|| request().cookies().get("AUTH_TOKEN").value().isEmpty()) return unauthorized(Json.parse("{\"unauthorized\": true}"));
		
		if(isLastActivitiesLoaded()){
			return ok(Json.toJson(DBController.getSkiActivities(request().cookies().get("ATHLETE_ID").value())));
		}else{
			syncStravaToDB();
			return ok(Json.parse("{\"NoActivities\": true}"));
		}
	}
	
	public Result getActivitiesSynced(){
		syncStravaToDB();
		return ok();
	}
	
	public Result getAthleteStatistics() {
		if(request().cookies().get("AUTH_TOKEN") != null 
				&& request().cookies().get("AUTH_TOKEN").value() == null 
				|| request().cookies().get("AUTH_TOKEN").value().isEmpty()) return unauthorized(Json.parse("{\"unauthorized\": true}"));
		
		return getAthleteStatisticsById(request().cookies().get("ATHLETE_ID").value());
		
	}
	
	public Result getAthleteStatisticsById(String Athlete_id) {
		List<ActivityModel> activities = DBController.getSkiActivities(Athlete_id);
		
		if(activities.isEmpty()){
			return ok(Json.parse("{\"isEmpty\": true}"));
		}else{
			AthleteStatistics statistics = new AthleteStatistics(DBController.getAthlete(Athlete_id).firstname);
			
			for (ActivityModel a : activities){
				statistics.addToTotalDistance(a.distance);
				statistics.collectLongestDay(a);
				statistics.collectYearsData(a);
				statistics.collectSeasonData(a);
				statistics.collectMaxSpeed(a);
			}
	
			if(DBController.getAthlete(Athlete_id).profile_medium.startsWith("http")){
				statistics.setProfileAvatar(DBController.getAthlete(Athlete_id).profile_medium);
				
			}
			
			return ok(statistics.asJson());
		}
	}
	
	public Result removeMe(){
		
		DBController.removeAthlete(request().cookies().get("ATHLETE_ID").value());
		return redirect("/logout");
	}
	
	public Result getFriends(){
		if(request().cookies().get("AUTH_TOKEN") == null 
				|| request().cookies().get("AUTH_TOKEN").value() == null 
				|| request().cookies().get("AUTH_TOKEN").value().isEmpty()) return unauthorized(Json.parse("{\"unauthorized\": true}"));
		
		final JStravaV3 strava = new JStravaV3(request().cookies().get("AUTH_TOKEN").value());
		List<Athlete> friends = strava.getCurrentAthleteFriends();
		return ok(Json.toJson(friends.stream().filter(a -> usesStravoski(a)).collect(Collectors.toList())));
	}
	
	private Boolean usesStravoski(Athlete a){
		if(DBController.getAthlete(Long.toString(a.getId())) == null){
			return false;
			}else{
				return true;
				}
		}
	
}
