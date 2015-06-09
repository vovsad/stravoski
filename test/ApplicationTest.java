import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;
//import javaguide.tests.controllers.Application;


import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;

import controllers.Application;

import org.junit.Test;

import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;
import play.twirl.api.Content;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest extends WithApplication {

	 @Override
	  protected FakeApplication provideFakeApplication() {
	    return new FakeApplication(new java.io.File("."), Helpers.class.getClassLoader(),
	        ImmutableMap.of("play.http.router", "javaguide.tests.Routes"), new ArrayList<String>(), null);
	  }
    

    @Test
    public void renderLogin() {
    	Result result = new Application().index();
        assertEquals(OK, result.status());
        assertEquals("text/html", result.contentType());
        assertTrue(contentAsString(result).contains("Stravoski uses Strava API to connect to your account"));
    }
    
    @Test
    public void getActivitiesJSON() {
    	Result result = new Application().getActivities();
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType());
    }


}


