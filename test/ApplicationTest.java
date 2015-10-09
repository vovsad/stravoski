import static org.junit.Assert.assertEquals;
import controllers.Application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import play.Logger;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Arrays;
import java.util.List;

import org.jstrava.entities.stream.Stream;

import com.google.gson.Gson;

import static org.mockito.Mockito.*;


public class ApplicationTest extends WithApplication {
	
	  @Override
	  protected FakeApplication provideFakeApplication() {
	    return new FakeApplication(new java.io.File("."), Helpers.class.getClassLoader(),
	    		ImmutableMap.of("play.http.router", "router.Routes"), new ArrayList<String>(), null);
	  }

	  
	@Test
    public void getActivityStream() throws Exception {
		getActivityStreamRunner("test/data/testData279129069.json", 8816, 16, 6);
		getActivityStreamRunner("test/data/testData279566942.json", 22905, 17, 15);
      }
	private void getActivityStreamRunner(String file, int distance, int grade, int slopes) throws Exception {
        Gson gson= new Gson();
		BufferedReader streamJsonData = new BufferedReader(
			new FileReader(file));

        Stream[] streamsArray=gson.fromJson(streamJsonData, Stream[].class);
        List<Stream> streams=Arrays.asList(streamsArray);
        
    	Application mockedApp = spy(Application.class);
    	doReturn(streams).when(mockedApp).getStream(-1);
    	
    	assertEquals(distance, mockedApp.getDownhillDistance(-1));
    	assertEquals(grade, mockedApp.getAverageGrade(-1));
    	assertEquals(slopes, mockedApp.getSlopesCount(-1));

	}



}