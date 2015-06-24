import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;
import controllers.Application;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import play.Logger;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;
import play.twirl.api.Content;


public class ApplicationTest extends WithApplication {
	


    @Test
    public void renderLogin() {
    	Content html = views.html.index.render(false);
    	assertEquals("text/html", html.contentType());
    	assertTrue(contentAsString(html).contains("Stravoski uses Strava API to connect to your account"));
    }
    
    @Test
    public void getActivitiesJSON() {
    	Result result = new Application().getActivities();
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());
    }


}