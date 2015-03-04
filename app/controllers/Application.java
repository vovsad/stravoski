package controllers;

import java.util.Calendar;
import java.util.TimeZone;

import models.Activities;

import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.libs.WS;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
	public static JsonNode athlete;
    	
    public static Result index() {
        return ok(views.html.index.render("Authorize Strava API Sample to connect to your account", Boolean.FALSE));
    }
    
    public static Result dashboard(String code)
    {
    	athlete = getAthlete(code).get(10000);
    	Logger.debug("athlete :: " + athlete.toString());
    	Activities.activities = getAthleteActivities(athlete.findValue("access_token").asText()).get(10000);
    	Logger.debug("activities :: " + Activities.activities.toString());
    	Logger.debug("seconds :: " + getSecondsSinceUnixEpoch());
    	return ok(views.html.index.render("You are authorized as " + 
    	athlete.findValue("firstname").asText() + " " + 
    			athlete.findValue("lastname").asText(), 
    			Boolean.TRUE));
    }

    //Client Secret:	22122cf967940aa0d142f51ca987b878aba948eb 
    public static Promise<JsonNode> getAthlete(String code) {
        return WS.url("https://www.strava.com/oauth/token")
        		.setContentType("application/x-www-form-urlencoded")
        		.post("client_id=" + 1455 + 
        				"&client_secret=" + "22122cf967940aa0d142f51ca987b878aba948eb" + 
        				"&code=" + code)
        		.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						return response.asJson();
					}
				});
    }

    public static Promise<JsonNode> getAthleteActivities(String access_token) {
    	Logger.debug("url :: " + "http://www.strava.com/api/v3/athlete/activities?" +
        		"after=" + getSecondsSinceUnixEpoch() +
				"&access_token=" + access_token);
		return WS.url("http://www.strava.com/api/v3/athlete/activities")
				.setQueryParameter("after", getSecondsSinceUnixEpoch())
				.setQueryParameter("access_token", access_token)
        		.get()
        		.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						Logger.debug("response :: " + response.getBody());
						return response.asJson();
					}
				});
    }
    
    private static String getSecondsSinceUnixEpoch(){
    	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	calendar.clear();
    	calendar.set(2014, Calendar.DECEMBER, 1);
    	return Long.toString(calendar.getTimeInMillis() / 1000L);
    }
    
}
