import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;
import controllers.Application;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;
import play.twirl.api.Content;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jstrava.entities.stream.Stream;
import org.junit.Before;
import org.mockito.Mockito;

import com.google.common.io.Files;
import com.google.gson.Gson;

import controllers.Application;
import data.streamJsonData;
import play.mvc.Http;
import play.test.FakeApplication;
import play.test.WithApplication;

import org.junit.Test;
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
    }



}