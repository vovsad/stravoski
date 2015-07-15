package controllers;

import java.util.List;

import models.ActivityModel;
import models.AthleteModel;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;

import play.Logger;
import play.mvc.Controller;

public class DBController extends Controller {
	
	public static String getMinActivityDate(String id){
		return Ebean
				.createSqlQuery(
						"select min(start_date) as min_start_date from activity_model where athlete_id =:athlete_id")
				.setMaxRows(1).setParameter("athlete_id", id).findUnique().getString("min_start_date");
	}

	public static String getMaxActivityDate(String id){
		return Ebean
				.createSqlQuery(
						"select max(start_date) as max_start_date from activity_model where athlete_id =:athlete_id")
				.setMaxRows(1).setParameter("athlete_id", id).findUnique().getString("max_start_date");
	}

	
	public static int cachedActivitiesCount(String id){
		return Ebean
				.createSqlQuery(
						"select count(*) as count from activity_model where athlete_id =:athlete_id")
						.setParameter("athlete_id", id).setMaxRows(1).findUnique().getInteger("count");
	}

	public static List<ActivityModel> getSkiActivities(String id) {
		return Ebean.find(ActivityModel.class).
				fetch("map").
				where("type ='AlpineSki' and athlete_id =:athlete_id").setParameter("athlete_id", id).
				orderBy("id desc").
				findList();
	}

	public static AthleteModel getAthlete(int id) {
		return Ebean.find(AthleteModel.class).
				where("id =:athlete_id").setParameter("athlete_id", id).findUnique();
		
	}
	
}
