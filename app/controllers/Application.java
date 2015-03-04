package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import models.Activities;
import models.Athlete;

import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.libs.WS;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
    	
    public static Result index() {
    	if (session("token") != null) 
    		return redirect("/dashboard");
        return ok(views.html.index.render("Authorize Strava API Sample to connect to your account", Boolean.FALSE));
    }
    
    public static Result tokenExchange(String code)
    {
    	Logger.debug("getAthlete");
    	if (Athlete.athlete == null) 
    		Athlete.athlete = WS.url("https://www.strava.com/oauth/token")
        		.setContentType("application/x-www-form-urlencoded")
        		.post("client_id=" + 1455 + 
        				"&client_secret=" + "22122cf967940aa0d142f51ca987b878aba948eb" + 
        				"&code=" + code)
        		.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						return response.asJson();
					}
				}).get(10000);
    	
    	session("token", Athlete.athlete.findValue("access_token").asText());
    	
    	return redirect("/dashboard");
    }
    
    public static Result dashboard()
    {
    	getAthlete(session("token"));
    	getAthleteActivities(session("token"));
    	
    	return ok(views.html.index.render("You are authorized as " + 
    	Athlete.athlete.findValue("firstname").asText() + " " + 
    	Athlete.athlete.findValue("lastname").asText(), 
    			Boolean.TRUE));
    }

    //Client Secret:	22122cf967940aa0d142f51ca987b878aba948eb 
    public static JsonNode getAthlete(String access_token) {
    	if (Athlete.athlete == null) 
    		Athlete.athlete = WS.url("https://www.strava.com//api/v3/athlete")
    			.setQueryParameter("access_token", access_token)
        		.get()
        		.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						return response.asJson();
					}
				}).get(10000);

    	return Athlete.athlete;
    }

    public static JsonNode getAthleteActivities(String access_token) {
    	if(Activities.activities == null)
    		Activities.activities = WS.url("http://www.strava.com/api/v3/athlete/activities")
				.setQueryParameter("after", getSecondsSinceUnixEpoch())
				.setQueryParameter("access_token", access_token)
        		.get()
        		.map(new Function<WS.Response, JsonNode>() {
					public JsonNode apply(WS.Response response) {
						Logger.debug("response :: " + response.getBody());
						return response.asJson();
					}
				}).get(10000);
    	return Activities.activities;
    }
    
    private static String getSecondsSinceUnixEpoch(){return getSecondsSinceUnixEpoch(null);}
    private static String getSecondsSinceUnixEpoch(Date date){
    	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	calendar.clear();
    	calendar.set(2014, Calendar.DECEMBER, 1);
    	return Long.toString(calendar.getTimeInMillis() / 1000L);
    }
    
}
