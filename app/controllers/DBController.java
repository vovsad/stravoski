package controllers;

import java.util.List;

import org.jstrava.entities.athlete.Athlete;

import models.ActivityModel;
import models.AthleteModel;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;

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

	public static AthleteModel getAthlete(String id) {
		return Ebean.find(AthleteModel.class).
				where("id =:athlete_id").setParameter("athlete_id", id).findUnique();
		
	}
	
	public static void removeAthlete(String id){
		SqlUpdate upd = Ebean.createSqlUpdate("delete from athlete_model, activity_model, polyline_model " + 
						"using athlete_model inner join activity_model inner join polyline_model " +
						"where athlete_model.id = activity_model.athlete_id and activity_model.map_id = polyline_model.id " +
						"and athlete_model.id=:id");
		upd.setParameter("id", id);
		upd.execute();
		
	}
	
}
