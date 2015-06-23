import org.junit.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import play.test.*;
import play.libs.F.*;
import static play.test.Helpers.*;
import static org.junit.Assert.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
    	
        running(testServer(3333, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver()/*HTMLUNIT*/, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                assertTrue(browser.pageSource().contains("Stravoski uses Strava API to connect to your account"));
            }
        });
    }

}
