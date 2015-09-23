import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import java.util.Collections;
import java.util.Map;

import models.ActivityModel;

import org.junit.Before;
import org.junit.Test;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

import controllers.Application;
import play.Logger;
import play.Play;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.WithApplication;
import play.twirl.api.Content;
import play.mvc.Http.RequestBuilder;
import static org.mockito.Mockito.*;


public class ApplicationTest extends WithApplication {
	public static FakeApplication app;
    private final Http.Request request = mock(Http.Request.class);
    
	@Before
	public void setUp() throws Exception {
	    Map<String, String> flashData = Collections.emptyMap();
	    Map<String, Object> argData = Collections.emptyMap();
	    Long id = 2L;
	    play.api.mvc.RequestHeader header = mock(play.api.mvc.RequestHeader.class);
	    Http.Context context = new Http.Context(id, header, request, flashData, flashData, argData);
	    Http.Context.current.set(context);
	}

    @Test
    public void renderLogin() {
    	Content html = views.html.index.render(false);
    	assertEquals("text/html", html.contentType());
    	assertTrue(contentAsString(html).contains("Stravoski uses Strava API to connect to your account"));
    }
    
    @Test
    public void renderIndex() {
    	Content html = views.html.index.render(true);
    	assertEquals("text/html", html.contentType());
    	assertTrue(contentAsString(html).contains("Your activities"));
    }
    
    @Test
    public void getActivitiesJSON() {
    	Logger.debug("getActivitiesJSON()");
    	RequestBuilder request = new RequestBuilder()
        .method(GET)
        .uri("/getactivities")
        .cookie(new Cookie("AUTH_TOKEN", Play.application().configuration().getString("stravoski.test.token"), 3600, "", "", false, true))
        .cookie(new Cookie("ATHLETE_ID", Play.application().configuration().getString("stravoski.test.athlete_id"), 3600, "", "", false, true));

    	Result result = route(request);
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());
        JsonNode resultAsJson = Json.parse(contentAsString(result));
        assertEquals(17, resultAsJson.size());

    }
    
    @Test
    public void getAthleteStatisticsJSON() {
    	Logger.debug("getAthleteStatisticsJSON()");
    	RequestBuilder request = new RequestBuilder()
        .method(GET)
        .uri("/getathletestat")
        .cookie(new Cookie("AUTH_TOKEN", Play.application().configuration().getString("stravoski.test.token"), 3600, "", "", false, true))
        .cookie(new Cookie("ATHLETE_ID", Play.application().configuration().getString("stravoski.test.athlete_id"), 3600, "", "", false, true));

    	Result result = route(request);
    	
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());

        JsonNode resultAsJson = Json.parse(contentAsString(result));
    	assertEquals(588950.75,
    			resultAsJson.get("totalDistance").asDouble(), 
    			0.001);

    }

    @Test
    public void isDataSyncedJSON() {
    	Logger.debug("isDataSyncedJSON()");
    	RequestBuilder request = new RequestBuilder()
        .method(GET)
        .uri("/isdatasynced")
        .cookie(new Cookie("AUTH_TOKEN", Play.application().configuration().getString("stravoski.test.token"), 3600, "", "", false, true))
        .cookie(new Cookie("ATHLETE_ID", Play.application().configuration().getString("stravoski.test.athlete_id"), 3600, "", "", false, true));

    	Result result = route(request);
    	
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());

        JsonNode resultAsJson = Json.parse(contentAsString(result));
    	assertEquals(true, resultAsJson.get("isDataSynced").asBoolean());

    }

    @Test
    public void isDataSyncedJSONNegative() {
    	Logger.debug("isDataSyncedJSONNegative()");
    	ActivityModel lastActivity = Ebean.find(ActivityModel.class).
		where("id = 348038961").
		findUnique();
    	if(lastActivity != null)
    		lastActivity.delete();
    	
    	
    	RequestBuilder request = new RequestBuilder()
        .method(GET)
        .uri("/isdatasynced")
        .cookie(new Cookie("AUTH_TOKEN", Play.application().configuration().getString("stravoski.test.token"), 3600, "", "", false, true))
        .cookie(new Cookie("ATHLETE_ID", Play.application().configuration().getString("stravoski.test.athlete_id"), 3600, "", "", false, true));

    	Result result = route(request);
    	
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());

        JsonNode resultAsJson = Json.parse(contentAsString(result));
    	assertEquals(false, resultAsJson.get("isDataSynced").asBoolean());
    	
    	request = new RequestBuilder()
        .method(GET)
        .uri("/dosync")
        .cookie(new Cookie("AUTH_TOKEN", Play.application().configuration().getString("stravoski.test.token"), 3600, "", "", false, true))
        .cookie(new Cookie("ATHLETE_ID", Play.application().configuration().getString("stravoski.test.athlete_id"), 3600, "", "", false, true));
    	result = route(request);

    }


}