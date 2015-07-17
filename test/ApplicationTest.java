import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import controllers.Application;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.core.j.JavaResultExtractor;
import play.mvc.Http;
import play.mvc.Http.RequestBody;
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
    	RequestBuilder request = new RequestBuilder()
        .method(GET)
        .uri("/getactivities")
        .session("Access_token", "722eb0a14c2e43220de11fa1d31e07bd52730294")
        .session("Athlete_id", "418242");

    	Result result = route(request);
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());
//      byte[] body = JavaResultExtractor.getBody(result, 0l);
//      Logger.debug(new String(body));
    }
    
    @Test
    public void getAthleteStatisticsJSON() {
    	RequestBuilder request = new RequestBuilder()
        .method(GET)
        .uri("/getathletestat")
        .session("Access_token", "722eb0a14c2e43220de11fa1d31e07bd52730294")
        .session("Athlete_id", "418242");

    	Result result = route(request);
    	
    	Logger.debug(contentAsString(result));
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());
    }


}