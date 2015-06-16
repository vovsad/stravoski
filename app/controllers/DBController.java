package controllers;

import com.avaje.ebean.Ebean;

import play.mvc.Controller;

public class DBController extends Controller {
	
	public static String getMinActivityDate(){
		return Ebean
				.createSqlQuery(
						"select min(start_date) as min_start_date from activity_model")
				.setMaxRows(1).findUnique().getString("min_start_date");
	}

	
	public static int cachedActivitiesCount(){
		return Ebean
				.createSqlQuery(
						"select count(*) as count from activity_model")
				.setMaxRows(1).findUnique().getInteger("count");
	}

}
