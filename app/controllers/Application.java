package controllers;

import org.jstrava.authenticator.AuthResponse;
import org.jstrava.authenticator.StravaAuthenticator;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.athlete.Athlete;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.*;
import play.libs.Json;
import play.libs.F.Function;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {
	private StravaAuthenticator authenticator;
	private AuthResponse authResponse;
	private JStravaV3 strava;
	private Athlete athlete;

    public Result index() {
        return ok(index.render(session("Access_token") != null &&
        		!session("Access_token").isEmpty()));
    }
    
	public Result tokenExchange(String code) {
		Logger.debug("in tokenExchange");
		
		authenticator = new StravaAuthenticator(
				1455, 
				"http://localhost:9000/tokenexchange",
				"22122cf967940aa0d142f51ca987b878aba948eb");
		
		authResponse = authenticator.getToken(code);
		
		session("Access_token", authResponse.getAccess_token());
		return ok(authResponse.getAccess_token()); //redirect("/dashboard");
	}
	
	public Result dashboard() {
		strava= new JStravaV3(session("Access_token"));
	    athlete=strava.getCurrentAthlete();
		
		return ok();
	}
	
	public Result login(){
		return redirect("https://www.strava.com/oauth/authorize?client_id=1455&redirect_uri=http://localhost:9000/tokenexchange&response_type=code");
	}
	
	public Result getActivities(){
		ObjectNode activities = Json.newObject();
		strava= new JStravaV3(session("Access_token"));
	    athlete=strava.getCurrentAthlete();

		return ok(strava.getCurrentAthleteActivities().toString());
	}
	
}
