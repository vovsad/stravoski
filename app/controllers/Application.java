package controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import models.Activities;
import models.Athlete;
import models.Seasons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.libs.WS;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import play.cache.Cache;

public class Application extends Controller {

	public static Result index() {
		if (session("token") != null)
			return redirect("/dashboard");
		return ok(views.html.index.render(
				"Authorize Strava API Sample to connect to your account",
				Boolean.FALSE));
	}

	public static Result tokenExchange(String code) {
		Logger.debug("getAthlete");
		if (Athlete.athlete == null)
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

		session("token", Athlete.athlete.findValue("access_token").asText());

		return redirect("/dashboard");
	}

	public static Result dashboard(String season) throws ParseException {
		if (season == null) {
			Activities.season = Seasons.season1415;
		}else{
			Activities.season = season;
		}
		if (((List<JsonNode>)Cache.get("Activities")) == null) {
			Cache.set("Activities", new LinkedList<JsonNode>());
		}
		
		getAthlete(session("token"));
		getAthleteActivities(session("token"));

		return ok(views.html.index.render("You are authorized as "
				+ Athlete.athlete.findValue("firstname").asText() + " "
				+ Athlete.athlete.findValue("lastname").asText(), Boolean.TRUE));
	}

	// Client Secret: 22122cf967940aa0d142f51ca987b878aba948eb
	public static JsonNode getAthlete(String access_token) {
		if (Athlete.athlete == null)
			Athlete.athlete = WS.url("https://www.strava.com//api/v3/athlete")
					.setQueryParameter("access_token", access_token).get()
					.map(new Function<WS.Response, JsonNode>() {
						public JsonNode apply(WS.Response response) {
							return response.asJson();
						}
					}).get(10000);

		return Athlete.athlete;
	}

	public static List<JsonNode> getAthleteActivities(String access_token) {
		
		Logger.debug("((List<JsonNode>) Cache.get(\"Activities\")).isEmpty()" + ((List<JsonNode>) Cache.get("Activities")).isEmpty());

		if (((List<JsonNode>) Cache.get("Activities")).isEmpty()) {
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
								List<JsonNode> a = (List<JsonNode>) Cache.get("Activities");
								a.add(activity);
								Cache.set("Activities", a);
							});

			new Thread("GetCachedActivities") {
				public void run() {
					Logger.debug("Thread: " + getName() + " running");
					cacheAthleteActivities(access_token, getLastCachedActivityDate());
				}
			}.start();

		}

		return ((List<JsonNode>) Cache.get("Activities"));
	}

	private static void cacheAthleteActivities(String token, String before) {
		Logger.debug("In cacheAthleteActivities for dates before " + before);
		WS.url("https://www.strava.com/api/v3/athlete/activities")
				.setQueryParameter("before", before)
				.setQueryParameter("access_token", token)
				.get()
				.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						return response.asJson();
					}
				})
				.get(10000)
				.forEach((activity) -> {
					List<JsonNode> a = (List<JsonNode>) Cache.get("Activities");
					a.add(activity);
					Cache.set("Activities", a);
				});
		Logger.debug( "Activities size in Cache is " + ((List<JsonNode>)Cache.get("Activities")).size());
		Logger.debug( "before.equals(getLastCachedActivityDate() " + before + " " +getLastCachedActivityDate());
		
		if(!before.equals(getLastCachedActivityDate()))
			cacheAthleteActivities(token, getLastCachedActivityDate());
		

	}

	public static Result getStatistics() {
		// getAthleteActivities(session("token"));
		return ok(Activities.getStatistics());
	}

	public static Result getActivityDetails(String id) {
		return WS.url("https://www.strava.com/api/v3/activities/" + id)
				.setQueryParameter("access_token", session("token")).get()
				.map(new Function<WS.Response, Result>() {
					public Result apply(WS.Response response) {
						return ok(response.asJson());
					}
				}).get(10000);

	}

	private static String getLastCachedActivityDate() {
		final String startdate = ((List<JsonNode>) Cache.get("Activities")).get(
				((List<JsonNode>) Cache.get("Activities")).size() - 1)
				.findValue("start_date").asText();
		
		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		try {
			return Long
					.toString(format.parse(startdate.substring(0, 10)).getTime() / 1000);
		} catch (ParseException e) {
			Logger.error("Cannot parse date from Strava date " + startdate);
			return null;
		}

	}
}
