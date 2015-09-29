import static org.junit.Assert.assertEquals;
import controllers.Application;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;
import java.util.Arrays;
import java.util.List;

import org.jstrava.entities.stream.Stream;

import com.google.gson.Gson;

import data.streamJsonData;

import static org.mockito.Mockito.*;


public class ApplicationTest extends WithApplication {
	
	  @Override
	  protected FakeApplication provideFakeApplication() {
	    return new FakeApplication(new java.io.File("."), Helpers.class.getClassLoader(),
	    		ImmutableMap.of("play.http.router", "router.Routes"), new ArrayList<String>(), null);
	  }

	  
	@Test
    public void getActivityStream() throws Exception {
        Gson gson= new Gson();
        Stream[] streamsArray=gson.fromJson(streamJsonData.data, Stream[].class);
        List<Stream> streams=Arrays.asList(streamsArray);
        
    	Application mockedApp = spy(Application.class);
    	doReturn(streams).when(mockedApp).getStream(-1);
    	
    	assertEquals(3343, mockedApp.getDownhillDistance(-1));
    	assertEquals(0.87399, mockedApp.getAverageGrade(-1), 0.001);
      }



}