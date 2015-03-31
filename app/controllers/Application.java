package controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import models.Activities;
import models.Athlete;
import models.Seasons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import play.libs.WS;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import play.cache.Cache;

public class Application extends Controller {

	public static Result index() {
		Logger.debug("in index");

		if (session("token") != null){
			return redirect("/dashboard");
		}
		return ok(views.html.index.render(
				"Authorize Strava API Sample to connect to your account",
				Boolean.FALSE));
	}

	public static Result tokenExchange(String code) {
		Logger.debug("in tokenExchange");
		
		if (session("token") == null) {
			Athlete.athlete = WS
					.url("https://www.strava.com/oauth/token")
					.setContentType("application/x-www-form-urlencoded")
					.post("client_id=" + 1455 + "&client_secret="
							+ "22122cf967940aa0d142f51ca987b878aba948eb"
							+ "&code=" + code)
					.map(new Function<WS.Response, JsonNode>() {
						public JsonNode apply(WS.Response response) {
							return response.asJson();
						}
					}).get(10000);
			Logger.debug(Athlete.athlete.toString());
			session("token", Athlete.athlete.findValue("access_token").asText());
		}
		session("AthleteID", Athlete.athlete.findValue("id").asText());

		return redirect("/dashboard");
	}

	public static Result dashboard(String season) throws ParseException {
		Logger.debug("in dashboard");

		if (season == null) {
			Activities.season = Seasons.season1415;
		} else {
			Activities.season = season;
		}

		getAthlete(session("token"));
		getAthleteActivities(session("token"));

		return ok(views.html.index.render("You are authorized as "
				+ Athlete.athlete.findValue("firstname").asText() + " "
				+ Athlete.athlete.findValue("lastname").asText(), Boolean.TRUE));
	}

	// Client Secret: 22122cf967940aa0d142f51ca987b878aba948eb
	public static JsonNode getAthlete(String access_token) {
		Logger.debug("in getAthlete");
		if (Athlete.athlete == null){
			Athlete.athlete = WS.url("https://www.strava.com//api/v3/athlete")
					.setQueryParameter("access_token", access_token).get()
					.map(new Function<WS.Response, JsonNode>() {
						public JsonNode apply(WS.Response response) {
							return response.asJson();
						}
					}).get(10000);

			session("AthleteID", Athlete.athlete.findValue("id").asText());
		}

		return Athlete.athlete;
	}

	public static List<JsonNode> getAthleteActivities(String access_token) {
		Logger.debug("in getAthleteActivities");

		if (Cache.get(session("AthleteID")) == null) {
			Cache.set(session("AthleteID"), new LinkedList<JsonNode>());
		}

		
		if (((List<JsonNode>) Cache.get(session("AthleteID"))).isEmpty()) {
			WS.url("https://www.strava.com/api/v3/athlete/activities")
					.setQueryParameter("access_token", access_token)
					.get()
					.map(new Function<WS.Response, JsonNode>() {
						public JsonNode apply(WS.Response response) {
							return response.asJson();
						}
					})
					.get(10000)
					.forEach(
							(activity) -> {
								List<JsonNode> a = (List<JsonNode>) Cache.get(session("AthleteID"));
								a.add(activity);
								Cache.set(session("AthleteID"), a);
							});

			// new Thread("GetCachedActivities") {
			// public void run() {
			// Logger.debug("Thread: " + getName() + " running");
			// cacheAthleteActivities(access_token,
			// getLastCachedActivityDate());
			// }
			// }.start();

		}

		return ((List<JsonNode>) Cache.get(session("AthleteID")));
	}

	private static void cacheAthleteActivities(String token, String before) {
		Logger.debug("In cacheAthleteActivities for dates before " + before);
		WS.url("https://www.strava.com/api/v3/athlete/activities")
				.setQueryParameter("before", before)
				.setQueryParameter("access_token", token).get()
				.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						return response.asJson();
					}
				}).get(10000).forEach((activity) -> {
					List<JsonNode> a = (List<JsonNode>) Cache.get(session("AthleteID"));
					a.add(activity);
					Cache.set(session("AthleteID"), a);
				});
		Logger.debug("Activities size in Cache is "
				+ ((List<JsonNode>) Cache.get(session("AthleteID"))).size());
		Logger.debug("before.equals(getLastCachedActivityDate() " + before
				+ " " + getLastCachedActivityDate());

		if (!before.equals(getLastCachedActivityDate()))
			cacheAthleteActivities(token, getLastCachedActivityDate());

	}

	public static Result getStatistics() {
		// getAthleteActivities(session("token"));
		return ok(Activities.getStatistics(session("AthleteID")));
	}

	public static Result getActivityDetails(String id) {
		return WS.url("https://www.strava.com/api/v3/activities/" + id)
				.setQueryParameter("access_token", session("token")).get()
				.map(new Function<WS.Response, Result>() {
					public Result apply(WS.Response response) {
						Logger.debug(response.asJson().toString());
						return ok(response.asJson());
					}
				}).get(10000);

	}

	public static Result getSkiMeanfullData(String id) {
		JsonNode streams = WS
				.url("https://www.strava.com/api/v3/activities/" + id
						+ "/streams/distance,altitude")
				.setQueryParameter("access_token", session("token")).get()
				.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						return response.asJson();
					}
				}).get(10000);
		// JsonNode streams =
		// WS.url("http://localhost:9000/assets/data/DistanceAltitude.json").get()

		List<SimpleEntry<Double, Double>> streamsDataOriginal = new LinkedList<>();
		// List<SimpleEntry<Double, Double>> streamsDataOnlyDownhill = new
		// LinkedList<>();

		Iterator<JsonNode> distance = streams.get(1).findValue("data")
				.iterator();
		Iterator<JsonNode> altitude = streams.get(0).findValue("data")
				.iterator();
		for (; altitude.hasNext() && distance.hasNext();) {
			streamsDataOriginal.add(new AbstractMap.SimpleEntry<>(distance
					.next().asDouble(), altitude.next().asDouble()));
		}

		Double previousAltitudePoint = 0.0, previousDistancePoint = 0.0, downhillDistance = 0.0;
		for (SimpleEntry<Double, Double> i : streamsDataOriginal) {

			if (i.getKey() <= previousAltitudePoint) {
				// streamsDataOnlyDownhill.add(i);
				downhillDistance += i.getValue() - previousDistancePoint;
			}
			previousAltitudePoint = i.getKey();
			previousDistancePoint = i.getValue();

		}

		ObjectNode recalculatedToSki = Json.newObject();
		recalculatedToSki.put("downhilled_distance",
				Math.round(downhillDistance) / 1000);
		Logger.debug(recalculatedToSki.toString());

		return ok(recalculatedToSki);
	}

	private static String getLastCachedActivityDate() {
		final String startdate = ((List<JsonNode>) Cache.get(session("AthleteID")))
				.get(((List<JsonNode>) Cache.get(session("AthleteID"))).size() - 1)
				.findValue("start_date").asText();

		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			return Long.toString(format.parse(startdate.substring(0, 10))
					.getTime() / 1000);
		} catch (ParseException e) {
			Logger.error("Cannot parse date from Strava date " + startdate);
			return null;
		}

	}
}
